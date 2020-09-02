package org.owpk.IODataHandler;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.owpk.message.Message;
import org.owpk.network.IONetworkServiceImpl;

import java.io.IOException;

public abstract class AbsHandler {
  private boolean handlerOver;

  protected void initDataListener() throws IOException, ClassNotFoundException {
    System.out.println("DataListener started :  " + this.getClass().toString() + " : handler: " + handlerOver);
    ObjectDecoderInputStream in = (ObjectDecoderInputStream) IONetworkServiceImpl.getService().getIn();
    Message<?> msg;
    while (!handlerOver) {
      if (in.available() > 0) {
        msg = (Message<?>) in.readObject();
        listen(msg);
      }
    }
  }

  protected void writeMessage(Message<?> message) throws IOException {
    ((ObjectEncoderOutputStream) IONetworkServiceImpl
        .getService()
        .getOut())
        .writeObject(message);
  }

  public void setHandlerOver(boolean handlerOver) {
    this.handlerOver = handlerOver;
  }
  protected abstract void listen(Message<?> message) throws IOException;
  public abstract void execute() throws InterruptedException, IOException, ClassNotFoundException;
}
