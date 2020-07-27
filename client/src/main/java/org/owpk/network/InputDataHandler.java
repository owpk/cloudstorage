package org.owpk.network;

import org.owpk.message.Messages;
import org.owpk.app.Callback;
import org.owpk.app.NetworkServiceInt;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class InputDataHandler implements Runnable {
  private Callback listViewCallback;
  private NetworkServiceInt networkServiceInt;

  private static final String USER_FOLDER = "./common/src/main/java/client/folder/";

  public InputDataHandler(NetworkServiceInt networkServiceInt, Callback... callbacks) throws IOException {
    this.networkServiceInt = networkServiceInt;
    System.out.println("-:input reader object stream initialized:");
  }

  @Override
  public void run() {
    try {
      System.out.println("-:input thread initialized:");
      Messages<?> msg;
      while (true) {
        msg = (Messages<?>) ((ObjectInputStream) networkServiceInt.getIn()).readObject();
        switch (msg.getType()) {
          case DIR:
            List<Path> dirList = (ArrayList<Path>) msg.getPayload();
            dirList.forEach(x -> System.out.println(x.getFileName()));
            break;
          case DOWNLOAD:
            String fileName = (String) msg.getPayload();
            System.out.println(fileName);
//            FileUtility.placeFile(networkServiceInt.getDataIn(),
//                new File(deleteThisHardCodePath + fileName));
            break;
          case OK:
            System.out.println(msg.getPayload());
          case ERROR:
            System.out.println(msg.getPayload());
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("-:oops server error:");
    } finally {
      try {
        networkServiceInt.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void parseCommand(String cmd) throws IOException {
    String[] rowCmd = cmd.split("\\s");
    if (rowCmd.length > 1) {
      if (rowCmd[0].equals(Commands.DOWNLOAD.getCmd()))
        System.out.println("download");
        //download(rowCmd);
      else if (rowCmd[0].equals(Commands.UPLOAD.getCmd()))
        System.out.println("up");
        //upload(cmd);
      else if (rowCmd[0].startsWith(Commands.DIR.getCmd()))
        printLog(Commands.DIR, Arrays.toString(rowCmd));
        //appendServerDirs(cmd);
    }
  }

//
//  private void upload(String cmd) throws IOException {
//    String fileName = FileUtility.getFileName(cmd.split(" "));
//    printLog(UPLOAD, fileName);
//    FileUtility.sendFile(os, new File(USER_FOLDER + fileName));
//  }

  private void appendServerDirs(String rowCmd) {
    printLog(Commands.DIR, rowCmd);
    List<String> serverFiles;
    serverFiles = Arrays.stream(rowCmd.split(":"))
        .skip(1)
        .collect(Collectors.toList());
    serverFiles.forEach(x -> listViewCallback.call(x));
  }

//  private void download(String[] rowCmd) throws IOException {
//    String fileName = FileUtility.getFileName(rowCmd);
//    printLog(DOWNLOAD, fileName);
//    FileUtility.placeFile(is, new File(USER_FOLDER + fileName));
//  }

  private String readBuffer(byte[] data, int off) {
    StringBuilder cmd = new StringBuilder();
    for (int i = 0; i < off; i++) {
      cmd.append((char) data[i]);
    }
    return cmd.toString();
  }

  private void printLog(Commands command, String payload) {
    System.out.println(String.format(
        "-:command: [%s] :payload: [%s]", command.getCmd(), payload));
  }

}
