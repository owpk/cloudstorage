package org.owpk.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class NetworkUtils {

  public static void sendObj(ObjectOutputStream out, Object o) throws IOException {
    out.writeObject(o);
    out.flush();
  }

}
