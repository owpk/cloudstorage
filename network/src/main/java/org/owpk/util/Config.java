package org.owpk.util;

import java.io.InputStream;
import java.util.Properties;

public class Config {
  private static String sourceRoot;
  private static String defaultServer;
  private static int port;
  static {
    Properties properties = new Properties();
    try (InputStream in =
             Config.class.getClassLoader()
                 .getResourceAsStream("app.properties")) {
      properties.load(in);
      sourceRoot = properties.getProperty("root_directory");
      defaultServer = properties.getProperty("default_server");
      port = checkPort(properties.getProperty("port"));
      System.out.println(sourceRoot);
    } catch (Exception e) {
      System.out.println("-:config read error");
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

  public static String getSourceRoot() {
    return sourceRoot;
  }

  public static void setSourceRoot(String sourceRoot) {
    Config.sourceRoot = sourceRoot;
  }

  public static String getDefaultServer() {
    return defaultServer;
  }

  public static void setDefaultServer(String defaultServer) {
    Config.defaultServer = defaultServer;
  }

  public static int getPort() {
    return port;
  }

  public static void setPort(int port) {
    Config.port = port;
  }
}
