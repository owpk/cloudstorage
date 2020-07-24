package org.owpk.fxClient;

import lombok.Data;
import org.owpk.util.ConfigReader;

import java.io.*;
import java.net.Socket;

@Data
public class Network {
  private String host;
  private int port;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private DataInputStream dataIn;
  private DataOutputStream dataOut;

  public Network() throws IOException {
    host = ConfigReader.getHost();
    port = ConfigReader.getPort();
  }

  public void initSocket() throws IOException {
    socket = new Socket(host, port);
    out = new ObjectOutputStream(socket.getOutputStream());
    in = new ObjectInputStream(socket.getInputStream());
    dataIn = new DataInputStream(socket.getInputStream());
    dataOut = new DataOutputStream(socket.getOutputStream());

  }

  public void onMessageThread(Runnable r) {
    Thread t = new Thread(r);
    t.setDaemon(true);
    t.start();
  }

}
