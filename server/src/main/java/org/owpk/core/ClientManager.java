package org.owpk.core;

import lombok.Data;
import lombok.SneakyThrows;
import org.owpk.auth.AuthService;
import org.owpk.command.ServerCommandHandler;
import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.util.Config;
import org.owpk.util.FileUtility;

import java.io.*;
import java.net.Socket;

@Data
public class ClientManager {

  private static int user_count;
  private int user_id;
  //TODO authService
  private AuthService authService;
  private Server server;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private DataInputStream dataIn;
  private DataOutputStream dataOut;
  private Socket socket;
  private final ServerCommandHandler serverCommandHandler;

  public ClientManager(Socket socket, Server server) {
    this.server = server;
    this.socket = socket;
    authService = new AuthService();
    try {
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
      dataIn = new DataInputStream(socket.getInputStream());
      dataOut = new DataOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    serverCommandHandler = new ServerCommandHandler(this);
  }

  public void manage() {
    //TODO authService
    if (authService.check()) {
      server.addAllowedUser(this);
      user_count++;
      user_id = user_count;
      createNewUserFolder();
      startSession();
    }
    else System.out.println("access denied");
  }

  private void createNewUserFolder() {
    try {
      FileUtility.createDirectory(getUserDirectory());
    } catch (IOException e) {
      System.out.println("Can't create dir");
      e.printStackTrace();
    }
  }

  @SneakyThrows
  private void startSession() {
    Messages<?> message;
    do {
        message = (Messages<?>) in.readObject();
        System.out.println(message.getType());
        serverCommandHandler.setMessage(message);
        serverCommandHandler.listen();
    } while (message.getType() != MessageType.CLOSE);
    server.deleteUser(this);
    socket.close();
  }

  public String getUserDirectory() {
    return Config.getSourceRoot()+"\\"+"1";
  }
}
