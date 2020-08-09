package org.owpk.network;

import org.owpk.IODataHandler.AuthException;
import org.owpk.app.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface NetworkServiceInt {
  void initHandlers(Callback... callbacks) throws IOException;
  void connect() throws IOException, ClassNotFoundException, InterruptedException, AuthException;
  void disconnect() throws IOException;

  boolean isRunning();
  String getName();
  OutputStream getOut();
  InputStream getIn();

}
