//package com.twitter.captainahab.main;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import com.twitter.captainahab.client.CaptainAhabClient;
//import com.twitter.captainahab.nemesis.NetworkPartitionManager;
//
//import static com.twitter.captainahab.main.ServerPortPair.listOfSetsToString;
//
//public class Main {
//  public static void main(String[] args) throws InterruptedException, IOException {
//
//    if (args[0].equals("client")) {
//      CaptainAhabClient.main(new String[] {args[1], args[2]});
//      System.exit(1);
//    }
//
//    ServerPortPair s1 = new ServerPortPair("10.247.2.101", 5005);
//    ServerPortPair s2 = new ServerPortPair("10.247.2.97", 5005);
//    ServerPortPair s4 = new ServerPortPair("10.247.2.186", 5005);
//    ServerPortPair s5 = new ServerPortPair("10.247.2.132", 5005);
//    ServerPortPair s6 = new ServerPortPair("10.247.2.107", 5005);
//
//
//    Set<ServerPortPair> allSet = new HashSet<>(Arrays.asList(s1, s2, s4, s5, s6));
//
//    Set<ServerPortPair> s1s2Set = new HashSet<>(Arrays.asList(s1, s2));
//    Set<ServerPortPair> s4s5Set = new HashSet<>(Arrays.asList(s4, s5));
//    Set<ServerPortPair> s5s6Set = new HashSet<>(Arrays.asList(s5, s6));
//    Set<ServerPortPair> s1s2s4Set = new HashSet<>(Arrays.asList(s1, s2, s4));
//    Set<ServerPortPair> s2s4s56St = new HashSet<>(Arrays.asList(s2, s4, s5, s6));
//    Set<ServerPortPair> s4s5s6set = new HashSet<>(Arrays.asList(s4, s5, s6));
//
//    CaptainAhabMain captain = new CaptainAhabMain(allSet, "kantoniadis");
//    captain.start();
//
//
////    // start ZK servers ...
////    captain.executeCommandToAllServers(allSet, "/tmp/zookeeper2/bin/zkServer.sh " +
////        "start /tmp/zookeeper2/zoo.cfg");
////    Thread.sleep(10 * 1000);
//
//
//    NetworkPartitionManager manager = captain.getNetworkPartitionManager();
//    List<Set<ServerPortPair>> topology = Arrays.asList(s1s2Set, s4s5s6set);
//
//
//    System.out.println("Changing topology to: " + listOfSetsToString(topology));
//    manager.changeTopology(Arrays.asList(s1s2Set, s4s5s6set));
//    System.out.println("Keep this topology for 30 seconds");
//    Thread.sleep(60 * 1000);
//
//    System.out.println("Changing topology to: " + listOfSetsToString(topology));
//    manager.changeTopology(Arrays.asList(allSet));
//    Thread.sleep(60 * 1000);
//
//
//
//    Thread.sleep(60 * 1000);
//    captain.stop();
//  }
//}
