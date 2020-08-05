package org.owpk.app;

import org.owpk.util.Config;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
/**
 * читает и перезаписывает файл {@link #CONFIG_NAME},
 * устанавливает последнюю посещенную диркеторию,
 * передает реализацию NetworkServiceInt для NetworkServiceFactory
 */
public class ClientConfig extends Config {
  private static final String CONFIG_NAME = "client.properties";
  private static final String DEFAULT_SERVER;
  private static Path startPath;

  static {
    initProp(CONFIG_NAME);
    DEFAULT_SERVER = properties.getProperty("default_server");
    port = checkPort(properties.getProperty("port"));
    startPath = Paths.get(properties.getProperty("root_directory"));
  }

  public static void setStartPath(String path) {
    startPath = Paths.get(path);
    try(FileWriter fw = new FileWriter(new File("client.properties"))) {
      properties.setProperty("root_directory", path);
      properties.store(fw, "root_directory");
      System.out.println(properties.getProperty("root_directory") + " <---- last visited directory");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getDefaultServer() {
    return DEFAULT_SERVER;
  }

  public static Path getStartPath() {
    return startPath;
  }
}
