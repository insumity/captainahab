package com.twitter.captainahab.utilities;

public class IptablesUtilities {

  public enum Chain {
    INPUT,
    OUTPUT
  }

  public enum Action {
    ADD("A"),
    DELETE("D");

    Action(String s) {
      action = s;
    }

    private String action;

    public String getAction() {
      return action;
    }
  }

  public enum Response {
    DROP, ACCEPT
  }

  public static String createIptablesCommand(Action action, Chain chain, String host, Response response) {
    String command;

    if (action == Action.ADD) {
      // always adds new rule at the beginning of the chain
      String template = "sudo iptables -I %s 1 -s %s -j %s";
      command = String.format(template, chain, host, response);
    }
    else {
      String template = "sudo iptables -%s %s -s %s -j %s";
      command = String.format(template, action.getAction(), chain, host, response);
    }
    return command;
  }

  public static String[] connectTo(String host) {
    String cmd1 = IptablesUtilities.createIptablesCommand(Action.ADD, Chain.INPUT, host, Response.ACCEPT);
    String cmd2 = IptablesUtilities.createIptablesCommand(Action.ADD, Chain.OUTPUT, host, Response.ACCEPT);

    String cmd3 = IptablesUtilities.createIptablesCommand(Action.DELETE, Chain.INPUT, host, Response.DROP);
    String cmd4 = IptablesUtilities.createIptablesCommand(Action.DELETE, Chain.OUTPUT, host, Response.DROP);

    return new String[] {cmd1, cmd2, cmd3, cmd4};
  }

  public static String[] disconnectFrom(String host) {
    String cmd1 = IptablesUtilities.createIptablesCommand(Action.ADD, Chain.INPUT, host, Response.DROP);
    String cmd2 = IptablesUtilities.createIptablesCommand(Action.ADD, Chain.OUTPUT, host, Response.DROP);

    String cmd3 = IptablesUtilities.createIptablesCommand(Action.DELETE, Chain.INPUT, host, Response.ACCEPT);
    String cmd4 = IptablesUtilities.createIptablesCommand(Action.DELETE, Chain.OUTPUT, host, Response.ACCEPT);

    return new String[] {cmd1, cmd2, cmd3, cmd4};
  }
}
