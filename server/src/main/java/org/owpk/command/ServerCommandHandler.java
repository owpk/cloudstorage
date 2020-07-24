package org.owpk.command;

import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.core.ClientManager;
import org.owpk.util.FileUtility;
import org.owpk.util.NetworkUtils;


import java.io.File;
import java.io.IOException;
import java.util.*;

public class ServerCommandHandler extends AbsCommandHandler {

  private final Map<String, Command> commandList;
  private final ClientManager clientManager;

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
//    commands.put("$download", this::downloadFileCmd);
    return commands;
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
    List<File> list = new ArrayList<>(FileUtility.showDirs(""));
    NetworkUtils.sendObj(clientManager.getOut(), new Messages<>(list, MessageType.DIR));
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
//  private void downloadFileCmd() throws IOException {
//    String filePath = parsePayload(rowCommand, 1);
//    FileUtility.downloadFile(clientManager.getOut(),filePath);
//  }

}
