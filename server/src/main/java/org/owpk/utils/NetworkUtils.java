package org.owpk.utils;

import org.owpk.Messages;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class NetworkUtils {

  public static void sendObj(OutputStream out, Object o) throws IOException {
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    objOut.writeObject(o);
  }

}
