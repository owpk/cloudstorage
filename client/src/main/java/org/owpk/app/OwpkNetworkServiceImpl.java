package org.owpk.app;

import java.io.*;
import java.net.Socket;

public class OwpkNetworkServiceImpl implements NetworkServiceInt {
  private String host;
  private int port;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private DataInputStream dataIn;
  private DataOutputStream dataOut;

  public OwpkNetworkServiceImpl(String host, int port) {
    this.port = port;
    this.host = host;
  }

  @Override
  public void connect() throws IOException {
      socket = new Socket(host, port);
      System.out.println("-:connected:");
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());
      dataIn = new DataInputStream(socket.getInputStream());
      dataOut = new DataOutputStream(socket.getOutputStream());
  }

  @Override
  public void disconnect() throws IOException {
    //TODO send exit code to server
    socket.close();
    out.close();
    out.flush();
    in.close();
    dataIn.close();
    dataOut.close();
    dataOut.flush();
    System.out.println("-:disconnected:");
  }

  @Override
  public void onMessageReceived(Runnable runnable) {
    Thread t = new Thread(runnable);
    t.setDaemon(true);
    t.start();
  }

  @Override
  public OutputStream getOut() {
    return out;
  }

  @Override
  public InputStream getIn() {
    return in;
  }
}
