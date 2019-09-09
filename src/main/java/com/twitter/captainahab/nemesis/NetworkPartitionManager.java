package com.twitter.captainahab.nemesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.twitter.captainahab.client.CaptainAhabClient;
import com.twitter.captainahab.main.ServerPortPair;
import com.twitter.captainahab.utilities.IptablesUtilities;

public class NetworkPartitionManager {

  private Map<String, CaptainAhabClient> serverToClient;
  private Set<ServerPortPair> servers;

  public NetworkPartitionManager(Map<String, CaptainAhabClient> serverToClient,
                                 Set<ServerPortPair> servers) {
    this.serverToClient = serverToClient;
    this.servers = servers;
  }

  private boolean checkComponents(List<Set<ServerPortPair>> components) {
    Set<ServerPortPair> allServers = new HashSet<>();
    for (Set<ServerPortPair> component : components) {
      for (ServerPortPair server : component) {
        if (allServers.contains(server)) {
          return false;
        }
        if (!servers.contains(server)) {
          return false;
        }
        allServers.add(server);
      }
    }

    return true;
  }

  public void changeTopology(List<Set<ServerPortPair>> components) {
    // verify that all the servers are provided and only these
    System.out.println("start - topology change [" + System.nanoTime() + "]");
    if (!checkComponents(components)) {
      System.err.println("Could not set the topology ...");
      throw new IllegalArgumentException("Bogus components: " +
          ServerPortPair.listOfSetsToString(components));
    }

    List<Thread> threads = new LinkedList<>();
    for (Set<ServerPortPair> component : components) {

      // easier to operate with lists in this case
      final List<ServerPortPair> lst = new ArrayList<>(component);

      for (int i = 0; i < lst.size(); ++i) {
        final List<String> hostsToConnect = new LinkedList<>();
        for (int j = 0; j < lst.size(); ++j) {
          if (j == i) {
            continue;
          }
          hostsToConnect.add(lst.get(j).getHost());
        }
        final int finalI = i;
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            connect(lst.get(finalI).getHost(), hostsToConnect);
          }
        });
        t.start();
        threads.add(t);
      }

      // disconnect servers with all other servers residing in different components
      Set<ServerPortPair> serversCopy = new HashSet<>();
      serversCopy.addAll(servers);
      serversCopy.removeAll(component);

      for (final ServerPortPair server : component) {
        final List<String> hostsToDisconnect = new LinkedList<>();
        for (ServerPortPair remServer : serversCopy) {
          hostsToDisconnect.add(remServer.getHost());
        }
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            disconnect(server.getHost(), hostsToDisconnect);
          }
        });
        t.start();
        threads.add(t);
      }
    }

    for (Thread t: threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("end - topology change [" + System.nanoTime() + "]");
  }

  private String disconnect(String host1, String host2) {
    StringBuffer result = new StringBuffer();

    CaptainAhabClient client1 = serverToClient.get(host1);
    String[] cmds1 = IptablesUtilities.disconnectFrom(host2);

    for (String cmd: cmds1) {
      String res = client1.applyCommand(cmd, true);
      result.append(res);
    }

    return result.toString();
  }

  private String disconnect(String host1, List<String> hosts) {
    if (hosts.size() == 0) {
      return "";
    }
    StringBuffer result = new StringBuffer();

    CaptainAhabClient client1 = serverToClient.get(host1);

    List<String> allCommands = new LinkedList<>();
    for (String host: hosts) {
      String[] cmds = IptablesUtilities.disconnectFrom(host);
      allCommands.addAll(Arrays.asList(cmds));
    }

    String oneBigCommand = new String();
    for (String cmd: allCommands) {
      oneBigCommand = oneBigCommand + cmd + "\n";
    }

    String res = client1.applyCommand(oneBigCommand, true);
    result.append(res);

    return result.toString();
  }

  private String connect(String host1, List<String> hosts) {
    if (hosts.size() == 0) {
      return "";
    }
    StringBuffer result = new StringBuffer();

    CaptainAhabClient client1 = serverToClient.get(host1);

    List<String> allCommands = new LinkedList<>();
    for (String host: hosts) {
      String[] cmds = IptablesUtilities.connectTo(host);
      allCommands.addAll(Arrays.asList(cmds));
    }

    String oneBigCommand = new String();
    for (String cmd: allCommands) {
      oneBigCommand = oneBigCommand + cmd + "\n";
    }

    String res = client1.applyCommand(oneBigCommand, true);
    result.append(res);

    return result.toString();
  }

  private String connect(String host1, String host2) {
    StringBuffer result = new StringBuffer();

    CaptainAhabClient client1 = serverToClient.get(host1);
    String[] cmds1 = IptablesUtilities.connectTo(host2);

    for (String cmd: cmds1) {
      String res = client1.applyCommand(cmd, true);
      result.append(res);
    }

    return result.toString();
  }

  public void clearIptables(Set<ServerPortPair> servers) {
    StringBuffer result = new StringBuffer();
    for (ServerPortPair server: servers) {
      CaptainAhabClient client = serverToClient.get(server.getHost());
      String res = client.applyCommand("sudo iptables -F", true);
      result.append(res);
    }
  }
}
