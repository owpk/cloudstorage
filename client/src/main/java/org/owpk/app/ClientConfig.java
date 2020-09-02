package org.owpk.app;

import javafx.application.Platform;
import org.owpk.controller.UserDialog;
import org.owpk.util.Config;

import java.io.File;
import java.nio.file.Path;

/**
 * Class to read and overwrite the file {@link #CONFIG_NAME}.
 * Sets the last visited directory.
 * Creates singleton {@link ClientConfig}.
 */
public class ClientConfig extends Config {
  private static final String CONFIG_NAME = "client.properties";
  private static final String DEFAULT_DOWNLOAD_DIR = "./Cloud Storage downloads";
  private static final String DEFAULT_SERVER = "localhost";
  private Path downloadDirectory;
  private Path startPath;
  private String host;
  private static ClientConfig config;

  public static ClientConfig getConfig() {
    if (config == null)
      config = new ClientConfig();
    return config;
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
    String propDwnld = properties.getProperty(ConfigParameters.DOWNLOAD_DIR.getDescription(), DEFAULT_DOWNLOAD_DIR);
    File f = new File(propDwnld);
    if (!f.exists()) {
      Platform.runLater(() -> {
        File temp = new File(DEFAULT_DOWNLOAD_DIR);
        temp.mkdirs();
        downloadDirectory = temp.toPath();
        writeProperty(ConfigParameters.DOWNLOAD_DIR, downloadDirectory.toAbsolutePath().toString());
        UserDialog.infoDialog("Could not find download directory","Default download folder created:\n" + temp.getAbsolutePath());
      });
    } else downloadDirectory = f.toPath();
    String strPath = properties.getProperty(ConfigParameters.LAST_DIR.getDescription());
    File temp = new File(strPath);
    if (strPath.isEmpty() || !temp.exists())
      startPath = File.listRoots()[0].toPath();
    else startPath = temp.toPath();
    port = checkPort(properties.getProperty(ConfigParameters.PORT.getDescription(), null));
    host = properties.getProperty(ConfigParameters.HOST.getDescription(), DEFAULT_SERVER);
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

  public static String getDefaultDownloadDir() {
    return DEFAULT_DOWNLOAD_DIR;
  }
}
