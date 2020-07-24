package org.owpk.fxClient;

import org.owpk.message.Messages;
import org.owpk.util.ConfigReader;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network {
  private String host;
  private int port;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;

  public Network() throws IOException {
    host = ConfigReader.getHost();
    port = ConfigReader.getPort();
    initSocket();
    onMessageThread();
  }

  private void initSocket() throws IOException {
    socket = new Socket(host, port);
    out = new ObjectOutputStream(socket.getOutputStream());
    in = new ObjectInputStream(socket.getInputStream());
  }


  private void onMessageThread() {
    Thread t = new Thread(() -> {
      try {
        while (true) {
          Messages<?> msg = (Messages<?>) in.readObject();
          switch (msg.getType()) {
            case DIR:
              List<File> dirList = (ArrayList) msg.getPayload();
              dirList.forEach(x -> System.out.println(x.getName()));
              break;
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });
    t.setDaemon(true);
    t.start();
  }

  public InputStream getIn() {
    return in;
  }

  public ObjectOutputStream getOut() {
    return out;
  }
}
