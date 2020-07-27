package org.owpk.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public abstract class AbsCommandHandler implements CommandHandler {
  protected final Map<String, Command> commandMap;
  protected String rowCommand;

  public AbsCommandHandler() {
    commandMap = fillCommands();
  }

  public Map<String, Command> getCommandMap() {
    return commandMap;
  }

  @Override
  public void listen() throws IOException {
    System.out.println(rowCommand);
    String cmd = parseCommand(rowCommand);
    try {
      if (commandMap.containsKey(cmd)) {
        commandMap.get(cmd).execute();
      } else {
        System.out.println("wrong command");
      }
    } catch (IOException e) {
      System.out.println("command error");
      e.printStackTrace();
    }
  }

  public String getRowCommand() {
    return rowCommand;
  }

  public void setRowCommand(String rowCommand) {
    this.rowCommand = rowCommand;
  }

  //Мапа команд
  protected abstract Map<String, Command> fillCommands();

  //Парсим команду
  protected static String parseCommand(String cmd) {
    return cmd.split("\\s")[0];
  }

  //Парсим нагрузку к команде
  protected static String parsePayload(String rowCmd) {
    return Arrays.stream(rowCmd.split("\\s"))
        .skip(1)
        .reduce("", (s1, s2) -> s1 + " " + s2)
        .trim();
  }

  protected static String parsePayload(String command, int region) {
    return command.split("\\s")[region];
  }

}
