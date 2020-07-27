package org.owpk.network;

import org.owpk.app.Config;

import java.util.HashMap;
import java.util.Map;

public class NetworkHandlerFactory {
  private static final Map<String, OwpkNetworkServiceImpl> map;
  static {
    map = new HashMap<>();
    map.put("localhost", new OwpkNetworkServiceImpl(Config.getDefaultServer(),Config.getPort()));
  }
  public static NetworkServiceInt getHandler(String server) {
    return map.get(server);
  }
}
