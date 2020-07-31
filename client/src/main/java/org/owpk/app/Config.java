package org.owpk.app;

import lombok.SneakyThrows;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class Config {
  private static Path sourceRoot;
  private static String defaultServer;
  private static int port;
  private static final Properties properties;
  static {
    properties = new Properties();
    initProp(properties);
    sourceRoot = Paths.get(properties.getProperty("root_directory"));
    if (!Files.exists(sourceRoot) || sourceRoot.toString().isEmpty()) {
      sourceRoot = FileSystemView.getFileSystemView().getDefaultDirectory().toPath();
      System.out.println("new default directory: " + sourceRoot.toString());
    }
    defaultServer = properties.getProperty("default_server");
    port = checkPort(properties.getProperty("port"));
  }

  private static void initProp(Properties prop) {
    try (InputStream in =
             new FileInputStream("./client.properties")) {
      prop.load(in);
    } catch (IOException e) {
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

  public static void setSourceRoot(String sourceRoot) {
    Config.sourceRoot = Paths.get(sourceRoot);
    try(FileWriter fw = new FileWriter(new File("client.properties"))) {
      properties.setProperty("root_directory", sourceRoot);
      properties.store(fw, "root_directory");
      System.out.println(properties.getProperty("root_directory") + " <---- last visited directory");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Path getSourceRoot() {
    return sourceRoot;
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
