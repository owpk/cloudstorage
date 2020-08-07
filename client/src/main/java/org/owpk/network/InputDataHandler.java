package org.owpk.network;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.owpk.app.Callback;
import org.owpk.app.ClientConfig;
import org.owpk.message.DataInfo;
import org.owpk.message.MessageType;
import org.owpk.message.Message;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * класс обработчик входных данных
 */
public class InputDataHandler implements Runnable {
  private NetworkServiceInt networkServiceInt;
  private Callback<String> serverStatusLabel;
  private Callback<List<FileInfo>> tableViewCallback;
  private final Map<String, DataInfo[]> files = new HashMap<>();

  public InputDataHandler(NetworkServiceInt networkServiceInt, Callback... callbacks) throws IOException {
    this.tableViewCallback = callbacks[0];
    this.serverStatusLabel = callbacks[1];
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

  //TODO download directory config
  private void download(DataInfo msg) throws IOException {
    FileUtility.assembleChunkedFile(msg, files, "C:\\Test\\out\\");
  }

}
