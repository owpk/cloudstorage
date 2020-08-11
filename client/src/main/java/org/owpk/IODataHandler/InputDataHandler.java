package org.owpk.IODataHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.util.Callback;
import org.owpk.app.ClientConfig;
import org.owpk.message.DataInfo;
import org.owpk.message.Message;
import org.owpk.network.IONetworkServiceImpl;
import org.owpk.network.NetworkServiceInt;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * класс обработчик входных данных
 */
public class InputDataHandler extends AbsHandler implements Runnable {
  private final Logger log = LogManager.getLogger(InputDataHandler.class.getName());
  private final Callback<String> serverStatusLabel;
  private final Callback<List<FileInfo>> tableViewCallback;
  private final Callback<Double> progressBarCallback;
  private final Callback<Path> refreshClientCallback;
  private final NetworkServiceInt networkServiceInt;


  public InputDataHandler(Callback... callbacks) throws IOException {
    this.tableViewCallback = callbacks[0];
    this.serverStatusLabel = callbacks[1];
    this.progressBarCallback = callbacks[2];
    this.refreshClientCallback = callbacks[3];
    this.networkServiceInt = IONetworkServiceImpl.getService();
  }

  @Override
  public void run() {
    try {
      log.info("thread started");
     initDataListener();
    } catch (IOException | ClassNotFoundException e) {
      log.error(e);
      serverStatusLabel.call("network error: " + ClientConfig.getDefaultServer());
      e.printStackTrace();
    } finally {
      try {
        handlerIsOver = true;
        networkServiceInt.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void listen(Message<?> msg) throws IOException {
    log.info(msg);
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
        log.error(msg.getPayload());
        break;
      case DEFAULT:
    }
  }

  private void download(DataInfo ms) throws IOException {
    String fileName = ms.getFile();
    final File f = new File(
          ClientConfig.getConfig().getDownloadDirectory().toString() + "\\" + fileName);
    FileUtility.FileWriter writer = FileUtility.FileWriter.getWriter(f.getAbsolutePath());
    writer.assembleChunkedFile(ms);
//    int chunkCount = ms.getChunkCount();
//    DataInfo[] data = files.get(fileName);
//    if (sessionIsOver(fileName)) {
//      final File f = new File(
//          ClientConfig.getConfig().getDownloadDirectory().toString() + "\\" + fileName);
//      FileUtility.writeBufferToFile(data, f);
//      progressBarCallback.call(0D);
//      serverStatusLabel.call("done");
//      refreshClientCallback.call(ClientConfig.getConfig().getDownloadDirectory().toAbsolutePath());
//      files.remove(fileName);
//    } else {
//      long percentage = Arrays.stream(data)
//          .filter(Objects::nonNull)
//          .count();
//      double count = (float) percentage / chunkCount;
//      progressBarCallback.call(count);
//    }
  }
}
