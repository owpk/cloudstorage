package org.owpk.network;

import com.sun.xml.internal.ws.util.QNameMap;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Класс {@link IONetworkServiceImpl} создающий подключение,
 * по умолчанию использует параметры из конфиг файла client.properties
 */
public class IONetworkServiceImpl implements NetworkServiceInt {
  private static final IONetworkServiceImpl service = new IONetworkServiceImpl(
      ClientConfig.getConfig().getHost(), ClientConfig.getConfig().getPort());

  private final Logger log = LogManager.getLogger(IONetworkServiceImpl.class.getName());
  private final String HOST;
  private final int PORT;
  private Socket socket;
  private ObjectDecoderInputStream in;
  private ObjectEncoderOutputStream out;
  private InputDataHandler inputDataHandler;
  private ConcurrentLinkedDeque<AbsHandler> pipeline;

  public IONetworkServiceImpl(String host, int port) {
    this.PORT = port;
    this.HOST = host;
  }

  public static IONetworkServiceImpl getService() {
    return service;
  }

  @Override
  public void initHandlers(Callback... callback) throws IOException {
    inputDataHandler = new InputDataHandler(
        callback[0],
        callback[1],
        callback[2],
        callback[3]);
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
    pipeline = new ConcurrentLinkedDeque<>();
    addHandlerToPipeline(new AuthHandler());
    for (int i = 0; i < pipeline.size(); i++) {
      pipeline.getLast().execute();
    }
  }

  @Override
  public void disconnect() {
    log.info("disconnected : " + HOST);
    pipeline.forEach(x -> x.setHandlerIsOver(true));
    try {
      socket.close();
      out.close();
      out.flush();
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addMainDataHandler() {
    pipeline.add(inputDataHandler);
  }

  public void addHandlerToPipeline(AbsHandler handler) {
    pipeline.add(handler);
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
