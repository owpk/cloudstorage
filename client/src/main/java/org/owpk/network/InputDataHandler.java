package org.owpk.network;

import org.owpk.app.Config;
import org.owpk.message.Messages;
import org.owpk.app.Callback;
import org.owpk.service.NetworkServiceInt;
import org.owpk.util.FileInfo;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * класс обработчик входных данных
 */
public class InputDataHandler implements Runnable {
  private Callback<List<FileInfo>> tableViewCallback;
  private NetworkServiceInt networkServiceInt;
  private Callback<String> serverStatusLabel;

  public InputDataHandler(NetworkServiceInt networkServiceInt, Callback... callbacks) throws IOException {
    this.tableViewCallback = callbacks[0];
    this.serverStatusLabel = callbacks[1];
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
            List<FileInfo> dirList = (List<FileInfo>) msg.getPayload();
            tableViewCallback.call(dirList);
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
      serverStatusLabel.call("server error " + Config.getDefaultServer());
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
    System.out.printf(
        "-:command: [%s] :payload: [%s]", command.getCmd(), payload);
  }

}
