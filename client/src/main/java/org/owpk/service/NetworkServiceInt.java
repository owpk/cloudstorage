package org.owpk.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface NetworkServiceInt {
  String getName();
  void connect() throws IOException;
  void disconnect() throws IOException;
  void initDataHandler(Runnable runnable);
  OutputStream getOut();
  InputStream getIn();

  //TODO read command, read file, write command, write file
}
