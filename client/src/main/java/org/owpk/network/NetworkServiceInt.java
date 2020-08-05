package org.owpk.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface NetworkServiceInt {
  void initDataHandler(Runnable r);

  String getName();
  void connect() throws IOException;
  void disconnect() throws IOException;

  OutputStream getOut();
  InputStream getIn();

  //TODO read command, read file, write command, write file
}
