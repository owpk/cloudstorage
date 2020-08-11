package org.owpk.network;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.IODataHandler.*;
import org.owpk.util.Callback;
import org.owpk.app.ClientConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  private InputDataHandler inputDataHandler;
  private AuthHandler authHandler;
  private SignHandler signHandler;
  private List<AbsHandler> pipeline;

  public IONetworkServiceImpl(String host, int port) {
    this.PORT = port;
    this.HOST = host;
  }

  public static NetworkServiceInt getService() {
    return service;
  }

  @Override
  public void initHandlers(Callback... callback) throws IOException {
    pipeline = new ArrayList<>();
    inputDataHandler = new InputDataHandler(
        callback[0],
        callback[1],
        callback[2],
        callback[3]);
    authHandler = new AuthHandler();
    signHandler = new SignHandler();
    pipeline.add(authHandler);
    pipeline.add(inputDataHandler);
  }

  @Override
  public String getName() {
    return HOST;
  }

  @Override
  public void connect() throws IOException, InterruptedException, ClassNotFoundException, AuthException {
    socket = new Socket(HOST, PORT);
    System.out.println("connected : " + socket.getRemoteSocketAddress());
    out = new ObjectEncoderOutputStream(socket.getOutputStream());
    in = new ObjectDecoderInputStream(socket.getInputStream());
    tryToAuthOrSign();
  }

  private void tryToAuthOrSign() throws InterruptedException, IOException, ClassNotFoundException {
    if (authHandler.tryToAuth()) { //sync
      if (signHandler.tryToSign()) { //sync
        authHandler = new AuthHandler();
        signHandler = new SignHandler();
        tryToAuthOrSign();
      }
    }
  }

  @Override
  public void disconnect() {
    log.info("disconnected : " + HOST);
    inputDataHandler.setHandlerIsOver(true);
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
  public OutputStream getOut() {
    return out;
  }

  @Override
  public InputStream getIn() {
    return in;
  }

}
