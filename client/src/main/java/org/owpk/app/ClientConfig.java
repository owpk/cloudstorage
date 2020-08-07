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
 * создает синглтон {@link ClientConfig}
 */
public class ClientConfig extends Config {
  private static final String CONFIG_NAME = "client.properties";
  private static final String DEFAULT_SERVER = "localhost";
  private Path downloadDirectory;
  private Path startPath;
  private String host;
  private static ClientConfig config;

  public static ClientConfig getConfig() {
    return config == null? new ClientConfig() : config;
  }

  private ClientConfig() {
    super(CONFIG_NAME);
  }

  @Override
  public void load() {
    downloadDirectory = Paths.get(properties.getProperty(ConfigParameters.DOWNLOAD_DIR.getDescription(), null));
    startPath = Paths.get(properties.getProperty(ConfigParameters.LAST_DIR.getDescription(), null));
    port = checkPort(properties.getProperty(ConfigParameters.PORT.getDescription(), null));
    host = properties.getProperty(ConfigParameters.HOST.getDescription(), DEFAULT_SERVER);
  }

  public void setDownloadDirectory(String path) {
    downloadDirectory = Paths.get(path);
    writeProperty(ConfigParameters.DOWNLOAD_DIR, path);
  }

  public void setStartPath(String path) {
    startPath = Paths.get(path);
    writeProperty(ConfigParameters.LAST_DIR, path);
  }

  public void writeProperty(ConfigParameters prop, String val) {
    try(FileWriter fw = new FileWriter(new File(CONFIG_NAME))) {
      properties.setProperty(prop.getDescription(), val);
      properties.store(fw, prop.getDescription());
      System.out.println(properties.getProperty(prop.getDescription()) + " -- " + prop.getDescription() + " : new property");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getDefaultServer() {
    return DEFAULT_SERVER;
  }

  public Path getDownloadDirectory() {
    return downloadDirectory;
  }

  public Path getStartPath() {
    return startPath;
  }

  public String getHost() {
    return host;
  }
}
