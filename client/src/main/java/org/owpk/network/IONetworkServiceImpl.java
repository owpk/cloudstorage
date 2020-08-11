package org.owpk.network;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.IODataHandler.AuthException;
import org.owpk.IODataHandler.AuthHandler;
import org.owpk.IODataHandler.InputDataHandler;
import org.owpk.IODataHandler.SignHandler;
import org.owpk.util.Callback;
import org.owpk.app.ClientConfig;

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
  private InputDataHandler inputDataHandler;
  private AuthHandler authHandler;
  private SignHandler signHandler;

  public IONetworkServiceImpl(String host, int port) {
    this.PORT = port;
    this.HOST = host;
  }

  public static NetworkServiceInt getService() {
    return service;
  }

  @Override
  public void initHandlers(Callback... callback) throws IOException {
    inputDataHandler = new InputDataHandler(
        callback[0],
        callback[1],
        callback[2],
        callback[3]);
    authHandler = new AuthHandler();
    signHandler = new SignHandler();
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
    new Thread(inputDataHandler).start();
  }

  private void tryToAuthOrSign() throws InterruptedException, IOException, ClassNotFoundException {
    authHandler.showDialog();
    if (authHandler.tryToAuth()) { //sync
      signHandler.showDialog();
      signHandler.tryToSign(); //sync
      authHandler = new AuthHandler();
      signHandler = new SignHandler();
      tryToAuthOrSign();
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
