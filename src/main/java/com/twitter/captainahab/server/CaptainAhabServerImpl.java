package com.twitter.captainahab.server;

import java.io.IOException;

import com.twitter.captainahab.CaptainAhabServiceGrpc;
import com.twitter.captainahab.Request;
import com.twitter.captainahab.Response;
import com.twitter.captainahab.utilities.Utilities;

import io.grpc.stub.StreamObserver;

public class CaptainAhabServerImpl extends CaptainAhabServiceGrpc.CaptainAhabServiceImplBase {

  @Override
  public void applyCommand(Request request, StreamObserver<Response> responseObserver) {
    String result;

    try {
      result = Utilities.executeCommandLocally(request.getCommand(), request.getWait());
    } catch (IOException e) {
      result = e.getMessage();
    }

    System.out.println(String.format("Received command: (%s)", request.getCommand()));

    Response response = Response.newBuilder().setResult(result).build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}


