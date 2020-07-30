package org.owpk.service;

import org.owpk.app.Config;
import org.owpk.service.NetworkServiceInt;
import org.owpk.service.OwpkNetworkServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс {@link OwpkNetworkServiceImpl} создающий подключение,
 * по умолчанию использует параметры из конфиг файла client.properties
 */
public class NetworkServiceFactory {
  private static final Map<String, OwpkNetworkServiceImpl> map;
  static {
    map = new HashMap<>();
    map.put("localhost", new OwpkNetworkServiceImpl(Config.getDefaultServer(),Config.getPort()));
  }
  public static NetworkServiceInt getHandler(String server) {
    return map.get(server);
  }
}
