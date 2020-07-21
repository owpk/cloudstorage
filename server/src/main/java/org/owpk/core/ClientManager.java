package org.owpk.core;

import org.owpk.auth.AuthService;
import org.owpk.command.CommandManager;
import org.owpk.utils.ConfigReader;
import org.owpk.utils.FileUtility;

import java.io.*;
import java.net.Socket;

public class ClientManager {
  private static int user_count;
  private int user_id;
  //TODO authService
  private AuthService authService;
  private Server server;
  private DataInputStream in;
  private DataOutputStream out;
  private Socket socket;
  private final CommandManager cmdListener;

  public ClientManager(Socket socket, Server server) {
    this.server = server;
    this.socket = socket;
    authService = new AuthService();
    try {
      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    cmdListener = new CommandManager(this);
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

  @lombok.SneakyThrows
  private void startSession() {
    String command = "";
    while (!command.equals("$close")) {
      command = in.readUTF();
      cmdListener.listen(command);
    }
    server.deleteUser(this);
    socket.close();
  }

  public DataInputStream getIn() {
    return in;
  }

  public DataOutputStream getOut() {
    return out;
  }

  public Socket getSocket() {
    return socket;
  }

  public static int getUser_count() {
    return user_count;
  }

  public int getUser_id() {
    return user_id;
  }

  public String getUserDirectory() {
    return ConfigReader.getDir()+"\\"+user_id;
  }
}
