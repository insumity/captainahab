package com.twitter.captainahab.main;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ServerPortPair {
  private String host;
  private int port;

  public ServerPortPair(String server, int port) {
    this.host = server;
    this.port = port;
  }

  public ServerPortPair() {

  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ServerPortPair) {
      ServerPortPair so = (ServerPortPair) o;
      return host.equals(so.host) && port == so.port;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port);
  }

  @Override
  public String toString() {
    return host + ":" + port;
  }

  public static String listOfSetsToString(List<Set<ServerPortPair>> components) {
    StringBuffer result = new StringBuffer();
    for (Set<ServerPortPair> lst: components) {
      result.append(" {");
      for (ServerPortPair s: lst) {
        result.append(s.getHost());
        result.append(", ");
      }

      // remove ", "
      result.deleteCharAt(result.length() - 1);
      result.deleteCharAt(result.length() - 1);
      result.append("} ");
    }
    return result.toString();
  }
}
