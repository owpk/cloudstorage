package org.owpk.service;

import org.owpk.app.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс {@link IONetworkServiceImpl} создающий подключение,
 * по умолчанию использует параметры из конфиг файла client.properties
 */
public class NetworkServiceFactory {
  private static final Map<String, IONetworkServiceImpl> map;
  static {
    map = new HashMap<>();
    map.put("localhost", new IONetworkServiceImpl(Config.getDefaultServer(),Config.getPort()));
  }
  public static NetworkServiceInt getHandler(String server) {
    return map.get(server);
  }
}
