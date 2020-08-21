package org.owpk.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.util.Config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to read and overwrite the file {@link #CONFIG_NAME}.
 * Sets the last visited directory.
 * Creates singleton {@link ClientConfig}.
 */
public class ClientConfig extends Config {
  private final Logger log = LogManager.getLogger(ClientConfig.class.getName());
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

  /**
   * load parameters in a config file client.properties,
   * sets the starting path, if it doesn't exists sets first parameter from filesystem roots list
   */
  @Override
  public void load() {
    downloadDirectory = Paths.get(properties.getProperty(ConfigParameters.DOWNLOAD_DIR.getDescription(), null));
    Path p = Paths.get(ConfigParameters.LAST_DIR.getDescription());
    if (!Files.exists(p)) {
      p = File.listRoots()[0].toPath();
    }
    startPath = Paths.get(properties.getProperty(p.toAbsolutePath().toString(), null));
    port = checkPort(properties.getProperty(ConfigParameters.PORT.getDescription(), null));
    host = properties.getProperty(ConfigParameters.HOST.getDescription(), DEFAULT_SERVER);
  }

  public void setDownloadDirectory(String path) {
    downloadDirectory = Paths.get(path);
    writeProperty(ConfigParameters.DOWNLOAD_DIR, path);
  }

  public void setStartPath(String path) {
    startPath = Paths.get(path);
    writeProperty(ConfigParameters.LAST_DIR, startPath.toAbsolutePath().toString());
  }

  /**
   * write property value to client.property
   */
  public void writeProperty(ConfigParameters prop, String val) {
    try(FileWriter fw = new FileWriter(new File(CONFIG_NAME))) {
      properties.setProperty(prop.getDescription(), val);
      properties.store(fw, prop.getDescription());
      log.info(properties.getProperty(prop.getDescription()) + " -- " + prop.getDescription() + " : new property");
    } catch (IOException e) {
      log.error(e);
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
