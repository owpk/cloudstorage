package org.owpk.fxClient;

import org.owpk.message.Messages;
import org.owpk.util.FileUtility;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OnReceiveMsg implements Runnable {

  private Network network;
  private Callback listViewCallback;
  private Callback getPathCallback;

  //TODO rework
  private String deleteThisHardCodePath = "C:\\Users\\vzvz4\\Desktop\\Test\\";

  public OnReceiveMsg(Network network, Callback... callback) {
    this.listViewCallback = callback[0];
    this.network = network;
  }

  @Override
  public void run() {
    try {
      Messages msg;
      while (true) {
          msg = (Messages) network.getIn().readObject();
          switch (msg.getType()) {
            case DIR:
              List<File> dirList = (ArrayList) msg.getPayload();
              dirList.forEach(x -> listViewCallback.call(x.getName()));
              break;
            case DOWNLOAD:
              String fileName = (String) msg.getPayload();
              System.out.println(fileName);
              FileUtility.placeFile(network.getDataIn(),
                  new File(deleteThisHardCodePath + fileName));
              break;
            case OK:
              System.out.println(msg.getPayload());
          }
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
