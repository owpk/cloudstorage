package org.owpk.command;

import org.owpk.core.ClientManager;
import org.owpk.utils.FileUtility;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {

  private final Map<String, Command> commandList;
  private static String rowCommand;
  private ClientManager cm;

  public CommandManager(ClientManager cm) {
    this.cm = cm;
    commandList = fillCommands();
  }

  public void listen(String command) {
    System.out.println(command);
    CommandManager.rowCommand = command;
    String cmd = parseCommand(rowCommand);
    try {
      if (commandList.containsKey(cmd)) {
        commandList.get(cmd).execute();
      }
    } catch (IOException e) {
      System.out.println("command error");
      e.printStackTrace();
    }
  }

  //Мапа команд
  private Map<String, Command> fillCommands() {
    Map<String, Command> commands = new HashMap<>();
    commands.put("$createF", this::createFileCmd);
    commands.put("$createD", this::createDirCmd);
    commands.put("$dir", this::showDirCmd);
    commands.put("$upload", this::uploadFileCmd);
    return commands;
  }

  //Парсим команду
  private static String parseCommand(String cmd) {
    return cmd.split(" ")[0];
  }

  //Парсим нагрузку к команде
  private static String parsePayload(String rowCmd) {
    return Arrays.stream(rowCmd.split(" "))
        .skip(1)
        .reduce("", (s1, s2) -> s1 + " " + s2)
        .trim();
  }

  private void closeConnection() {
    try {
      cm.getSocket().close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createFileCmd() throws IOException {
    String file = parsePayload(rowCommand);
    System.out.println(file);
    FileUtility.createFile(cm.getUserDirectory() + "\\" + file);
  }

  private void createDirCmd() throws IOException {
    String dirName = parsePayload(rowCommand);
    FileUtility.createDirectory(cm.getUserDirectory() + "\\" + dirName);
  }

  private void showDirCmd() {
    FileUtility.showDirs("")
        .forEach(x -> {
          try {
            cm.getOut().writeUTF(x.getName());
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  private void uploadFileCmd() throws IOException {
    String fileName = Arrays.stream(parsePayload(rowCommand)
        .split("\\\\"))
        .reduce((first, second) -> second)
        .orElse(null);
    System.out.println(fileName);
    File file = new File(cm.getUserDirectory() + "\\" + fileName);
    file.createNewFile();
    FileUtility.placeUploadedFile(cm.getIn(), file);
  }

}
