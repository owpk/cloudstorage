package org.owpk.core;

import lombok.Data;
import lombok.SneakyThrows;
import org.owpk.auth.AuthService;
import org.owpk.command.ServerCommandHandler;
import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.util.ConfigReader;
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
  private Socket socket;
  private final ServerCommandHandler cmdListener;

  public ClientManager(Socket socket, Server server) {
    this.server = server;
    this.socket = socket;
    authService = new AuthService();
    try {
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    cmdListener = new ServerCommandHandler(this);
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
    Messages<?> command;
    do {
      command = (Messages<?>) in.readObject();
      System.out.println(command.getType());
      cmdListener.listen(command.getType().getCmd());
    } while (command.getType() != MessageType.CLOSE);
    server.deleteUser(this);
    socket.close();
  }

  public String getUserDirectory() {
    return ConfigReader.getDir()+"\\"+user_id;
  }
}
