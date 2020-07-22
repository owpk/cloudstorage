package org.owpk.command;

import org.owpk.MessageType;
import org.owpk.Messages;
import org.owpk.core.ClientManager;
import org.owpk.utils.FileUtility;
import org.owpk.utils.NetworkUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager {

  private final Map<String, Command> commandList;
  private String rowCommand;
  private final ClientManager clientManager;

  public CommandManager(ClientManager cm) {
    this.clientManager = cm;
    commandList = fillCommands();
  }

  public void listen(String command) {
    System.out.println(command);
    this.rowCommand = command;
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
    commands.put("$download", this::downloadFileCmd);
    return commands;
  }

  //Парсим команду
  private static String parseCommand(String cmd) {
    return cmd.split("\\s")[0];
  }

  //Парсим нагрузку к команде
  private static String parsePayload(String rowCmd) {
    return Arrays.stream(rowCmd.split("\\s"))
        .skip(1)
        .reduce("", (s1, s2) -> s1 + " " + s2)
        .trim();
  }

  private static String parsePayload(String command, int region) {
    return command.split("\\s")[region];
  }

  private void closeConnection() {
    try {
      clientManager.getSocket().close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createFileCmd() throws IOException {
    String file = parsePayload(rowCommand);
    System.out.println(file);
    FileUtility.createFile(clientManager.getUserDirectory() + "\\" + file);
  }

  private void createDirCmd() throws IOException {
    String dirName = parsePayload(rowCommand);
    FileUtility.createDirectory(clientManager.getUserDirectory() + "\\" + dirName);
  }

  private void showDirCmd() throws IOException {
    List<String> dirs =  FileUtility
        .showDirs("")
        .stream()
        .map(File::getName)
        .collect(Collectors.toList());
    NetworkUtils.sendObj(clientManager.getOut(), new Messages(dirs, MessageType.DIR));
  }

  private void uploadFileCmd() throws IOException {
    String fileName = Arrays.stream(parsePayload(rowCommand)
        .split("\\\\"))
        .reduce((first, second) -> second)
        .orElse(null);
    System.out.println(fileName);
    File file = new File(clientManager.getUserDirectory() + "\\" + fileName);
    file.createNewFile();
    FileUtility.placeUploadedFile(clientManager.getIn(), file);
  }

  private void downloadFileCmd() throws IOException {
    String filePath = parsePayload(rowCommand, 1);
    FileUtility.downloadFile(clientManager.getOut(),filePath);
  }

}
