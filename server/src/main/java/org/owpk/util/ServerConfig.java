package org.owpk.util;

public class ServerConfig extends Config {
  private static final String CONFIG_NAME = "server.properties";

  public ServerConfig() {
    super(CONFIG_NAME);
  }

  @Override
  public void load() {
    port = checkPort(properties.getProperty(ConfigParameters.PORT.getDescription(), null));
  }

  public int getPort() {
    return port;
  }
}
