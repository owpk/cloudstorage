package org.owpk.IODataHandler;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import org.owpk.message.Message;

import java.io.IOException;

public abstract class AbsHandler {
  protected boolean handlerIsOver;
  protected void initDataListener() throws IOException, ClassNotFoundException {
    ObjectDecoderInputStream in = (ObjectDecoderInputStream) IONetworkServiceImpl.getService().getIn();
    Message<?> msg;
    while (!handlerIsOver) {
      if (in.available() > 0) {
        msg = (Message<?>) in.readObject();
        listen(msg);
      }
    }
  }

  public void setHandlerIsOver(boolean handlerIsOver) {
    this.handlerIsOver = handlerIsOver;
  }
  protected abstract void listen(Message<?> message) throws IOException;
}
