package org.owpk.network;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.owpk.IODataHandler.AbsHandler;
import org.owpk.IODataHandler.AuthHandler;
import org.owpk.IODataHandler.InputDataHandler;
import org.owpk.app.ClientConfig;
import org.owpk.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * The {@link IONetworkServiceImpl} class creating a connection,
 * by default uses parameters from the client.properties config file.
 * Creates a pipeline of {@link AbsHandler} as listeners for messages from the server,
 * the first time you connect, the default is to add {@link AuthHandler} as the first listener
 * @see #connect()
 * @see #executePipeline()
 */
public class IONetworkServiceImpl implements NetworkServiceInt {
  private static final IONetworkServiceImpl service = new IONetworkServiceImpl(
      ClientConfig.getConfig().getHost(), ClientConfig.getConfig().getPort());

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
    inputDataHandler = new InputDataHandler(callback);
  }

  @Override
  public String getName() {
    return HOST;
  }

  /**
   * Creates a socket, InputStream, OutputStream, and pipeline, adds {@link AuthHandler} to the pipeline
   */
  @Override
  public void connect() throws IOException, InterruptedException, ClassNotFoundException {
    socket = new Socket(HOST, PORT);
    System.out.println("connected : " + socket.getRemoteSocketAddress());
    out = new ObjectEncoderOutputStream(socket.getOutputStream());
    in = new ObjectDecoderInputStream(socket.getInputStream());
    pipeline = new ConcurrentLinkedDeque<>();
    addHandlerToPipeline(new AuthHandler());
    executePipeline();
  }

  @Override
  public void disconnect() {
    if (socket != null) {
      if (pipeline != null) clearPipeline();
      try {
        socket.close();
        out.close();
        out.flush();
        in.close();
        System.out.println("disconnected: " + HOST);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void executePipeline() throws InterruptedException, IOException, ClassNotFoundException {
    for (int i = 0; i < pipeline.size(); i++) {
      pipeline.getLast().execute();
    }
  }

  public void addMainDataHandler() {
    pipeline.add(inputDataHandler);
  }

  public void addHandlerToPipeline(AbsHandler handler) {
    pipeline.add(handler);
  }

  public void clearPipeline() {
    pipeline.forEach(x -> x.setHandlerOver(true));
    pipeline.clear();
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
