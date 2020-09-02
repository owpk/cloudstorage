package org.owpk.util;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Properties;

/**
 * Initialize {@link Properties}
 */
public abstract class Config {
  private static final String LINE_SEPARATOR = FileSystems.getDefault().getSeparator();;
  static {
    System.out.println(LINE_SEPARATOR);
  }
  protected static final Properties properties;
  private final String propName;
  protected int port;
  static {
    properties = new Properties();
  }

  public enum ConfigParameters {
    PORT("port"),
    HOST("host"),
    LAST_DIR("last_directory"),
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
    this.propName = propName;
    initProp(propName);
    load();
  }

  /**
   * write property value to client.property
   */
  public void writeProperty(ConfigParameters prop, String val) {
    try {
      File f = new File("./" + propName);
      try(FileWriter fw = new FileWriter(f.getAbsolutePath())) {
        properties.setProperty(prop.getDescription(), val);
        properties.store(fw, prop.getDescription());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
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

  public static String getLineSeparator() {
    return LINE_SEPARATOR;
  }
}
