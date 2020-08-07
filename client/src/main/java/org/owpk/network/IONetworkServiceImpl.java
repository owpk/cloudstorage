package org.owpk.network;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.owpk.app.Callback;
import org.owpk.app.ClientConfig;
import org.owpk.message.DataInfo;
import org.owpk.message.MessageType;
import org.owpk.message.Message;
import org.owpk.util.FileInfo;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class IONetworkServiceImpl implements NetworkServiceInt {
  private final String HOST;
  private final int PORT;
  private Socket socket;
  private ObjectDecoderInputStream in;
  private ObjectEncoderOutputStream out;

  public IONetworkServiceImpl(String host, int port) {
    this.PORT = port;
    this.HOST = host;
  }

  @Override
  public void initDataHandler(Runnable r) {
    Thread t = new Thread(r);
    t.setDaemon(true);
    t.start();
  }

  @Override
  public String getName() {
    return "localhost";
  }

  @Override
  public void connect() throws IOException {
      socket = new Socket(HOST, PORT);
      System.out.println("-:connected:");
      out = new ObjectEncoderOutputStream(socket.getOutputStream());
      in = new ObjectDecoderInputStream(socket.getInputStream());
  }

  @Override
  public void disconnect() throws IOException {
    if (socket != null) {
      out.writeObject(new Message<>(MessageType.CLOSE, ""));
      socket.close();
      out.close();
      out.flush();
      in.close();
      System.out.println("-:disconnected:");
    }
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
