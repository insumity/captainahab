package com.twitter.captainahab.server;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class CaptainAhabServer {

  private Server server;
  private int port;

  public CaptainAhabServer(int port) {
    this.port = port;
    server = ServerBuilder.forPort(port).addService(new CaptainAhabServerImpl()).build();
  }

  public void start() {
    System.out.println("Server about to start in port: " + port);
    try {
      server.start();

      server.awaitTermination();
    } catch (IOException | InterruptedException e) {
      System.err.println("Server error: " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    if (args.length != 1) {
      throw new IllegalArgumentException("Wrong command line arguments. Execute as follows " +
          "with \"./main portNumber\"");
    }

    int port = Integer.parseInt(args[0]);
    new CaptainAhabServer(port).start();
  }
}
