package org.owpk.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConfig extends Config {
  private static final String CONFIG_NAME = "server.properties";
  private Path root;
  private static ServerConfig config;

  public static ServerConfig getConfig() {
    return config == null ? new ServerConfig() : config;
  }

  private ServerConfig() {
    super(CONFIG_NAME);
  }

  @Override
  public void load() {
    port = checkPort(properties.getProperty(ConfigParameters.PORT.getDescription(), null));
    root = Paths.get(properties.getProperty(ConfigParameters.SERVER_ROOT.getDescription(), null));
  }

  public Path getRoot() {
    return root;
  }

  public int getPort() {
    return port;
  }
}
