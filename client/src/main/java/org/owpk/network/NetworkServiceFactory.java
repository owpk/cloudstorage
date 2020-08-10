package org.owpk.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика классов для взаимодействия с облачными хранилищами
 */
public class NetworkServiceFactory {
  private static final Map<String, NetworkServiceInt> map;
  static {
    map = new HashMap<>();
    map.put("localhost", IONetworkServiceImpl.getService());
  }
  public static NetworkServiceInt getHandler(String server) {
    return map.get(server);
  }
}
