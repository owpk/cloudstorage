package org.owpk.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
  private static Path sourceRoot;
  private static String defaultServer;
  private static int port;
  static {
    Properties properties = new Properties();
    try (InputStream in =
             Config.class.getClassLoader()
                 .getResourceAsStream("app.properties")) {
      properties.load(in);
    } catch (IOException e) {
      e.printStackTrace();
    }

    sourceRoot = Paths.get(properties.getProperty("root_directory"));
    if (!Files.exists(sourceRoot))
      sourceRoot = FileSystems.getDefault().getRootDirectories().iterator().next();
    defaultServer = properties.getProperty("default_server");
    port = checkPort(properties.getProperty("port"));

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

  public static Path getSourceRoot() {
    return sourceRoot;
  }

  public static void setSourceRoot(Path sourceRoot) {
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
