package org.owpk.IODataHandler;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;
import org.owpk.network.IONetworkServiceImpl;

import java.io.IOException;

public abstract class AbsHandler {
  protected boolean handlerIsOver;

  protected void initDataListener() throws IOException, ClassNotFoundException {
    System.out.println("DataListener started :  " + this.getClass().toString());
    ObjectDecoderInputStream in = (ObjectDecoderInputStream) IONetworkServiceImpl.getService().getIn();
    Message<?> msg;
    while (!handlerIsOver) {
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

  public void setHandlerIsOver(boolean handlerIsOver) {
    this.handlerIsOver = handlerIsOver;
  }
  protected abstract void listen(Message<?> message) throws IOException;
  public abstract void execute() throws InterruptedException, IOException, ClassNotFoundException;
}
