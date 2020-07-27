package org.owpk.network;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;

public class FileUtility {

  public static void createFile(String fileName) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) {
      file.createNewFile();
    }
  }

  public static void createDirectory(String dirName) throws IOException {
    File file = new File(dirName);
    if (!file.exists()) {
      file.mkdir();
    }
  }

  public static void placeFile(InputStream is, File file) throws IOException {
    byte [] buffer = new byte[8192];
    try(FileOutputStream fos = new FileOutputStream(file)) {
        while (is.available() > 0) {
          int count = is.read(buffer);
          fos.write(buffer, 0, count);
        }
    }
  }

  public static void placeFile(SelectableChannel ch, File file) throws IOException {
    byte [] buffer = new byte[8192];
    try(FileOutputStream fos = new FileOutputStream(file)) {
      for (long i = 0; i < buffer.length; i++) {
        int count = ((SocketChannel)ch).read(ByteBuffer.wrap(buffer));
        fos.write(buffer, 0, count);
      }
    }
  }

  public static void sendFile(OutputStream os, File file) throws IOException {
    try(FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[8192];
      while (fis.available() > 0) {
        int count = fis.read(buffer);
        os.write(buffer, 0, count);
      }
    }
  }

  public static void sendFile(SelectableChannel ch, File file) throws IOException {
    try(FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[8192];
      while (fis.available() > 0) {
        fis.read(buffer);
        ((SocketChannel)ch).write(ByteBuffer.wrap(buffer));
      }
    }
  }

  public static List<File> showDirs(String currentDir) {
    File dir = new File(currentDir);
    return Arrays.asList(dir.listFiles());
  }

  public static String getFileName(String... cmd) {
    return Arrays.stream(cmd)
        .skip(1)
        .reduce("", (x, y) -> x + " " + y)
        .trim();
  }

}
