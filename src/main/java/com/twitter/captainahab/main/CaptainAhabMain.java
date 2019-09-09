package com.twitter.captainahab.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.twitter.captainahab.client.CaptainAhabClient;
import com.twitter.captainahab.nemesis.NetworkPartitionManager;
import com.twitter.captainahab.utilities.Utilities;

public class CaptainAhabMain {

  private NetworkPartitionManager manager;
  private Set<ServerPortPair> servers;
  private String username;
  private Map<String, CaptainAhabClient> serverToClient;

  private static final String JAR_PATH = "/tmp/captainahab.jar";

  public CaptainAhabMain(Set<ServerPortPair> servers, String username) {
    this.servers = servers;
    this.username = username;
  }

  private void transferServerJar(Set<ServerPortPair> servers, String username) {
    for (ServerPortPair server: servers) {
      try {
        Utilities.scpTo(JAR_PATH, "/tmp", username, server.getHost());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void startServers(Set<ServerPortPair> servers, String username) {
    for (ServerPortPair server: servers) {

      try {
        // first kill a running captainahab server (just in case)
        Utilities.executeCommandRemotely(server.getHost(), username, "sudo pkill -f captainahab");

        String startCommand = "sudo java -jar /tmp/captainahab.jar 5005 " +
            "1>/tmp/captainahab_server_stdout.log " +
            "2>/tmp/captainahab_server_stderr.log &";
        Utilities.executeCommandRemotely(server.getHost(), username, startCommand);
        System.out.println(">> Started server " + server.getHost() + " at " + server.getPort());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void killServers(Set<ServerPortPair> servers) {
    for (ServerPortPair server: servers) {
      CaptainAhabClient client = serverToClient.get(server.getHost());
      client.applyCommand("sudo pkill -f captainahab", false);
    }
  }

  private Map<String, CaptainAhabClient> startClients(Set<ServerPortPair> servers) {
    Map<String, CaptainAhabClient> serverToClient = new HashMap<>();
    for (ServerPortPair server: servers) {
      CaptainAhabClient client = new CaptainAhabClient();
      client.start(server.getHost(), server.getPort());
      serverToClient.put(server.getHost(), client);
      System.out.println(">> Started client " + client);
    }
    return serverToClient;
  }

  public void start() throws InterruptedException {
    transferServerJar(servers, username);
    startServers(servers, username);
    // dirty FIXME
    Thread.sleep(5 * 1000);
    System.out.println(">> Servers started.");


    serverToClient = startClients(servers);
    // dirty FIXME
    Thread.sleep(5 * 1000);

    System.out.println(">> Clients started.");
  }

  public NetworkPartitionManager getNetworkPartitionManager() {
    if (serverToClient == null) {
      throw new IllegalStateException("CaptainAhabMain needs to start first!");
    }

    manager = new NetworkPartitionManager(serverToClient, servers);
    return manager;
  }

  public void stop()  {
    if (manager != null) {
      manager.clearIptables(servers);
    }
    killServers(servers);
  }

  public void executeCommandToAllServers(Set<ServerPortPair> servers, String command) {
    for (ServerPortPair server: servers) {
      CaptainAhabClient client = serverToClient.get(server.getHost());
      client.applyCommand(command, false);
    }
  }
}
