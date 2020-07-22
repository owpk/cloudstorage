package org.owpk;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.owpk.MessageType.DIR;

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
            sc.close();
            break;
          }
          if (cmd.startsWith("$")) {
            if (cmd.startsWith("$upload")) {
              upload(cmd.substring(7).trim(), out);
            } else if (cmd.startsWith("$download")) {
              download(parsePayload(cmd, 1), parsePayload(cmd, 2), in);
            }
          }
          cmd = sc.nextLine();
          out.writeUTF(cmd);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
    readObj(new ObjectInputStream(socket.getInputStream()));
  }

  private static void readObj(ObjectInputStream in) {
    Thread t = new Thread(() -> {
      try {
        Messages<?> msg = (Messages<?>) in.readObject();
        while (true) {
          switch (msg.getType()) {
            case DIR :
              List<String> dirList = (ArrayList) msg.getPayload();
              dirList.forEach(System.out::println);
              msg.setType(MessageType.OK);
              break;
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });
    t.setDaemon(true);
    t.start();
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
    System.out.println(outPath);
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
