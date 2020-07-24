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
  }

  public void initSocket() throws IOException {
    socket = new Socket(host, port);
    out = new ObjectOutputStream(socket.getOutputStream());
    in = new ObjectInputStream(socket.getInputStream());
  }

  public void onMessageThread(Runnable r) {
    Thread t = new Thread(r);
    t.setDaemon(true);
    t.start();
  }

  public ObjectInputStream getIn() {
    return in;
  }

  public ObjectOutputStream getOut() {
    return out;
  }
}
