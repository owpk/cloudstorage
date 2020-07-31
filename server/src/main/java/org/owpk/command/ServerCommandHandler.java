package org.owpk.command;

import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.core.ClientManager;

import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;
import org.owpk.util.NetworkUtils;
import sun.nio.ch.Net;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ServerCommandHandler extends AbsCommandHandler {

  private final Map<String, Command> commandList;
  private final ClientManager clientManager;
  private Messages message;

  public ServerCommandHandler(ClientManager cm) {
    this.clientManager = cm;
    commandList = fillCommands();
  }


  @Override
  protected Map<String, Command> fillCommands() {
    Map<String, Command> commands = new HashMap<>();
    commands.put("$createF", this::createFileCmd);
    commands.put("$createD", this::createDirCmd);
    commands.put("$dir", this::showDirCmd);
//    commands.put("$upload", this::uploadFileCmd);
    commands.put("$download", this::downloadFileCmd);
    commands.put("$close", this::close);
    return commands;
  }

  private void close() {
    try {
      clientManager.getSocket().close();
      System.out.println("close");
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
    String path =  message.getPayload() == null ? "" : (String) message.getPayload();
    if (path.isEmpty()) path = "C:\\";
    List<FileInfo> list = new ArrayList<>(FileUtility.showDirs(path));
    NetworkUtils.sendObj(clientManager.getOut(), new Messages<>(MessageType.DIR, list));
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
      NetworkUtils.sendObj(clientManager.getOut(), new Messages<>(MessageType.OK, "Starting download..."));
      NetworkUtils.sendObj(clientManager.getOut(), new Messages<>(MessageType.DOWNLOAD, message.getPayload()));
      FileUtility.sendFile(clientManager.getDataOut(), file);
    } else NetworkUtils.sendObj(clientManager.getOut(), new Messages<>(MessageType.ERROR, "File doesn't exist"));
  }

  public Messages getMessage() {
    return message;
  }

  public void setMessage(Messages message) {
    rowCommand = message.getType().getCmd();
    this.message = message;
  }
}
