package org.owpk.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * инициализирует {@link Properties}
 */
public abstract class Config {
  protected static final Properties properties;
  protected int port;
  static {
    properties = new Properties();
  }

  public enum ConfigParameters {
    PORT("port"),
    HOST("default_server"),
    LAST_DIR("root_directory"),
    CONNECT_ON_STARTUP("connect_on_startup"),
    DOWNLOAD_DIR("download_directory"),
    SERVER_ROOT("root_folder");
    private final String description;

    ConfigParameters(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public Config(String propName) {
    initProp(propName);
    load();
  }

  public abstract void load();

  private void initProp(String propName) {
    try (InputStream in =
             new FileInputStream("./"+propName)) {
      Config.properties.load(in);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected int checkPort(String port) {
    int p = 0;
    try {
      p = Integer.parseInt(port);
    } catch (NumberFormatException nfe) {
      System.out.println("check port");
    }
    return p;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
