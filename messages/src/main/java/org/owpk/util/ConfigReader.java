package org.owpk.util;

import java.io.InputStream;
import java.util.Properties;


public class ConfigReader {

  private static String sourceRoot;
  private static int port;
  private static String host;

  static {
    Properties properties = new Properties();
    try (InputStream in =
             ConfigReader.class.getClassLoader()
                 .getResourceAsStream("app.properties")) {
      properties.load(in);
      host = properties.getProperty("host");
      sourceRoot = properties.getProperty("rootDirectory");
      port = checkPort(properties.getProperty("port"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static int checkPort(String port) {
    int p;
    try {
      p = Integer.parseInt(port);
    } catch (NumberFormatException nfe) {
      throw new NumberFormatException("check port");
    }
    return p;
  }

  public static String getDir() {
    return sourceRoot;
  }

  public static int getPort() {
    return port;
  }

  public static String getHost() {
    return host;
  }
}
