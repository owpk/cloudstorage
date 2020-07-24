package org.owpk.command;

import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.core.ClientManager;
import org.owpk.util.FileUtility;
import org.owpk.util.NetworkUtils;
import sun.nio.ch.Net;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ServerCommandHandler extends AbsCommandHandler {

  private final Map<String, Command> commandList;
  private final ClientManager clientManager;
  private Messages message;

  public ServerCommandHandler(ClientManager cm) {
    this.clientManager = cm;
    commandList = fillCommands();
  }

  //Мапа команд
  @Override
  protected Map<String, Command> fillCommands() {
    Map<String, Command> commands = new HashMap<>();
    commands.put("$createF", this::createFileCmd);
    commands.put("$createD", this::createDirCmd);
    commands.put("$dir", this::showDirCmd);
//    commands.put("$upload", this::uploadFileCmd);
    commands.put("$download", this::downloadFileCmd);
    return commands;
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
    List<File> list = new ArrayList<>(FileUtility.showDirs(""));
    NetworkUtils.sendObj(clientManager.getOut(), new Messages(list, MessageType.DIR));
  }

//  private void uploadFileCmd() throws IOException {
//    String fileName = Arrays.stream(parsePayload(rowCommand)
//        .split("\\\\"))
//        .reduce((first, second) -> second)
//        .orElse(null);
//    System.out.println(fileName);
//    File file = new File(clientManager.getUserDirectory() + "\\" + fileName);
//    file.createNewFile();
//    FileUtility.placeUploadedFile(clientManager.getIn(), file);
//  }
//

  private void downloadFileCmd() throws IOException {
    System.out.println(message.getPayload());
    File file = new File(clientManager.getUserDirectory() + "\\" + message.getPayload());
    if (file.exists()) {
      NetworkUtils.sendObj(clientManager.getOut(), new Messages("Starting download...", MessageType.OK));
      NetworkUtils.sendObj(clientManager.getOut(), new Messages(message.getPayload(), MessageType.DOWNLOAD));
      FileUtility.sendFile(clientManager.getDataOut(), file);
    } else NetworkUtils.sendObj(clientManager.getOut(), new Messages("File doesn't exist", MessageType.ERROR));
  }

  public Messages getMessage() {
    return message;
  }

  public void setMessage(Messages message) {
    rowCommand = message.getType().getCmd();
    this.message = message;
  }
}
