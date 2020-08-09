package org.owpk.IODataHandler;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import org.owpk.message.Message;

import java.io.IOException;

public abstract class AbsHandler {
  protected void initListener(Message<?> msg) throws IOException, ClassNotFoundException {
    ObjectDecoderInputStream in = (ObjectDecoderInputStream) IONetworkServiceImpl.getService().getIn();
    while (true) {
      if (in.available() > 0)
        listen(msg);
    }
  }

  protected abstract void listen(Message<?> message);
}
