package org.owpk.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * инициализирует {@link Properties}
 */
public class Config {
  protected static int port;
  protected static final Properties properties;
  static {
    properties = new Properties();
  }

  protected static void initProp(String propName) {
    try (InputStream in =
             new FileInputStream("./"+propName)) {
      Config.properties.load(in);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected static int checkPort(String port) {
    int p = 0;
    try {
      p = Integer.parseInt(port);
    } catch (NumberFormatException nfe) {
      System.out.println("check port");
    }
    return p;
  }

  public static int getPort() {
    return port;
  }

  public static void setPort(int port) {
    Config.port = port;
  }
}
