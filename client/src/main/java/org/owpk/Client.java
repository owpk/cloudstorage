package org.owpk;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

  public static void main(String[] args) throws IOException {
    Scanner sc = new Scanner(System.in);
    Socket socket = new Socket("localhost", 8183);
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    DataInputStream in = new DataInputStream(socket.getInputStream());
    System.out.println("Welcome!");
    new Thread(() -> {
      String cmd = "";
      try {
        while (true) {
          if (cmd.equals("exit")) {
            socket.close();
            out.writeUTF("$close");
            sc.close(); break;
          }

          if (cmd.startsWith("$upload")) {
            upload(cmd.substring(7).trim(), out);
          }

          cmd = sc.nextLine();
          out.writeUTF(cmd);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      while (true) {
        try {
          System.out.println(in.readUTF());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  public static void upload(String path, DataOutputStream os) throws IOException {
    File file = new File(path);
    InputStream is = new FileInputStream(file);
    byte[] buffer = new byte[8192];
    while (is.available() > 0) {
      int readBytes = is.read(buffer);
      os.write(buffer, 0, readBytes);
    }

  }
}
