package org.owpk.core;

import org.owpk.util.Config;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
  private static int port;
  //TODO close connection logic
  private boolean isActive = true;
  private Set<ClientManager> connections;

  public Server() {
    port = Config.getPort();
    connections = new HashSet<>();
  }

  public void run() {
    ExecutorService es = Executors.newCachedThreadPool();
    try (ServerSocket srv = new ServerSocket(port)) {
      System.out.println("server started");
      while (isActive) {
        Socket socket = srv.accept();
        System.out.println("accepted: " + socket.getRemoteSocketAddress());
        es.execute(() -> new ClientManager(socket, this).manage());
      }
    } catch (IOException e) {
      System.out.println("server error");
    }
  }

  public void addAllowedUser(ClientManager cm) {
    connections.add(cm);
  }

  public void deleteUser(ClientManager clientManager) {
    connections.remove(clientManager);
  }
}
