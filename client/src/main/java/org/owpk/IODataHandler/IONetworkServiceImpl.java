package org.owpk.IODataHandler;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.app.Callback;
import org.owpk.app.ClientConfig;
import org.owpk.network.NetworkServiceInt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Класс {@link IONetworkServiceImpl} создающий подключение,
 * по умолчанию использует параметры из конфиг файла client.properties
 */
public class IONetworkServiceImpl implements NetworkServiceInt {
  private static final NetworkServiceInt service = new IONetworkServiceImpl(
      ClientConfig.getConfig().getHost(), ClientConfig.getConfig().getPort());

  private final Logger log = LogManager.getLogger(IONetworkServiceImpl.class.getName());
  private final String HOST;
  private final int PORT;
  private Socket socket;
  private ObjectDecoderInputStream in;
  private ObjectEncoderOutputStream out;
  private boolean run;
  private InputDataHandler inputDataHandler;
  private AuthHandler authHandler;

  public IONetworkServiceImpl(String host, int port) {
    this.PORT = port;
    this.HOST = host;
  }

  public static NetworkServiceInt getService() {
    return service;
  }

  @Override
  public void initCallBacks(Callback... callback) throws IOException {
    inputDataHandler = new InputDataHandler(
        callback[0],
        callback[1],
        callback[2],
        callback[3]);
    authHandler = new AuthHandler();
  }

  @Override
  public String getName() {
    return HOST;
  }

  @Override
  public void connect() throws IOException, InterruptedException, ClassNotFoundException {
    socket = new Socket(HOST, PORT);
    log.info("connected : " + socket.getRemoteSocketAddress());
    out = new ObjectEncoderOutputStream(socket.getOutputStream());
    in = new ObjectDecoderInputStream(socket.getInputStream());
    run = true;
    authHandler.showDialog();
    authHandler.tryToAuth();
    new Thread(inputDataHandler).start();
  }

  @Override
  public void disconnect() {
    run = false;
    log.info("disconnected : " + HOST);
    try {
      socket.close();
      out.close();
      out.flush();
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean isRunning() {
    return run;
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
