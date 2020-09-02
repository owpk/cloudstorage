package org.owpk.network;

import org.owpk.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface NetworkServiceInt {
  void initHandlers(Callback... callbacks) throws IOException;
  void connect() throws IOException, ClassNotFoundException, InterruptedException;
  void disconnect();

  String getName();
  OutputStream getOut();
  InputStream getIn();

}
