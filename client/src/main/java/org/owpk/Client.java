package org.owpk;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
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
          if (cmd.startsWith("$")) {
            if (cmd.startsWith("$upload")) {
              upload(cmd.substring(7).trim(), out);
            }

            else if (cmd.startsWith("$download")) {
              download(parsePayload(cmd,1), parsePayload(cmd, 2), in);
            }
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

  private static String parsePayload(String command, int region) {
    return command.split("\\s")[region].trim();
  }

  public static void download(String inPath, String outPath, DataInputStream is) throws IOException {
    String fileName = Arrays.stream(inPath
        .split("\\\\"))
        .reduce((first, second) -> second)
        .orElse(null);
    System.out.println(fileName);
    File f = new File(outPath + "\\" + fileName);
    f.createNewFile();
    try (FileOutputStream fos = new FileOutputStream(f)) {
      byte[] buffer = new byte[8192];
      while (true) {
        int r = is.read(buffer);
        if (r == -1) break;
        fos.write(buffer, 0, r);
      }
    }
  }
}
