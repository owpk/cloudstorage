package org.owpk.util;

public class ServerConfig extends Config {
  private static final String CONFIG_NAME = "server.properties";
  static {
    initProp(CONFIG_NAME);
    port = checkPort(properties.getProperty("port"));
  }

  public static int getPort() {
    return port;
  }
}
