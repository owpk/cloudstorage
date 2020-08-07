package org.owpk.network;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import org.owpk.app.Callback;
import org.owpk.app.ClientConfig;
import org.owpk.message.DataInfo;
import org.owpk.message.Message;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * класс обработчик входных данных
 */
public class InputDataHandler implements Runnable {

  private final NetworkServiceInt networkServiceInt;
  private final Callback<String> serverStatusLabel;
  private final Callback<List<FileInfo>> tableViewCallback;
  private final Callback<Double> progressBarCallback;
  private final Callback<Path> refreshClientCallback;
  private final Map<String, DataInfo[]> files = new HashMap<>();

  public InputDataHandler(NetworkServiceInt networkServiceInt, Callback... callbacks) throws IOException {
    this.tableViewCallback = callbacks[0];
    this.serverStatusLabel = callbacks[1];
    this.progressBarCallback = callbacks[2];
    this.refreshClientCallback = callbacks[3];
    this.networkServiceInt = networkServiceInt;
  }

  @Override
  public void run() {
    try {
      System.out.println("-:input thread init:");
      Message<?> msg;
      ObjectDecoderInputStream in = (ObjectDecoderInputStream) networkServiceInt.getIn();
      while (true) {
        if (in.available() > 0) {
          msg = (Message<?>) in.readObject();
          System.out.println(msg);
          switch (msg.getType()) {
            case DIR:
              List<FileInfo> dirList = (List<FileInfo>) msg.getPayload();
              tableViewCallback.call(dirList);
              break;
            case DOWNLOAD:
              download((DataInfo) msg);
              break;
            case OK:
            case ERROR:
              System.out.println(msg.getPayload());
              break;
            case DEFAULT:
          }
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("-:oops server error:");
      serverStatusLabel.call("server error " + ClientConfig.getDefaultServer());
      e.printStackTrace();
    } finally {
      try {
        networkServiceInt.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean sessionIsOver(String fileName) {
    return Arrays.stream(files.get(fileName)).allMatch(Objects::nonNull);
  }

  private void download(DataInfo ms) throws IOException {
    FileUtility.assembleChunkedFile(ms, files);
    String fileName = ms.getFile();
    int chunkCount = ms.getChunkCount();
    DataInfo[] data = files.get(fileName);
    if (data != null) {
      long percentage = Arrays.stream(data)
          .filter(Objects::nonNull)
          .count();
      double count = (float) percentage / chunkCount;
      progressBarCallback.call(count);
      if (sessionIsOver(fileName)) {
        final File f = new File(
            ClientConfig.getConfig().getDownloadDirectory().toString() + "\\" + fileName);
        FileUtility.writeBufferToFile(data, f);
        progressBarCallback.call(0D);
        serverStatusLabel.call("done");
        refreshClientCallback.call(ClientConfig.getConfig().getDownloadDirectory().toAbsolutePath());
      }
    }
  }
}
