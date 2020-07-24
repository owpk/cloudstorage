package org.owpk.fxClient;

import org.owpk.message.Messages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OnReceiveMsg implements Runnable {

  private Network network;
  private Callback callback;

  public OnReceiveMsg(Network network, Callback callback) {
    this.callback = callback;
    this.network = network;
  }

  @Override
  public void run() {
    try {
      while (true) {
        Messages<?> msg = (Messages<?>) network.getIn().readObject();
        switch (msg.getType()) {
          case DIR:
            List<File> dirList = (ArrayList) msg.getPayload();
            dirList.forEach(x -> callback.call(x.getName()));
            break;
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
