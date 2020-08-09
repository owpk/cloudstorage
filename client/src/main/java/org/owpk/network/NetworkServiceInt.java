package org.owpk.network;

import org.owpk.app.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface NetworkServiceInt {
  void initCallBacks(Callback... callbacks) throws IOException;
  void connect() throws IOException, ClassNotFoundException, InterruptedException;
  void disconnect() throws IOException;

  boolean isRunning();
  String getName();
  OutputStream getOut();
  InputStream getIn();

  //TODO read command, read file, write command, write file
}
