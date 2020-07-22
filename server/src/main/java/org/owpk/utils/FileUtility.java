package org.owpk.utils;

import java.io.*;
import java.net.Socket;
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

  public static List<File> showDirs(String currentDir) {
    File dir = new File(ConfigReader.getDir() + "/" + currentDir);
    return Arrays.asList(dir.listFiles());
  }

  public static void move(File dir, File file) throws IOException {
    String path = dir.getAbsolutePath() + "/" + file.getName();
    createFile(path);
    InputStream is = new FileInputStream(file);
    try (OutputStream os = new FileOutputStream(new File(path))) {
      byte[] buffer = new byte[8192];
      while (is.available() > 0) {
        int readBytes = is.read(buffer);
        System.out.println(readBytes);
        os.write(buffer, 0, readBytes);
      }
    }
  }

  public static void placeUploadedFile(DataInputStream is, File file) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      byte[] buffer = new byte[8192];
      while (true) {
        int r = is.read(buffer);
        if (r == -1) break;
        fos.write(buffer, 0, r);
      }
    }
  }

  public static void downloadFile(DataOutputStream os, String path) throws IOException {
    File file = new File(path);
    InputStream is = new FileInputStream(file);
    byte[] buffer = new byte[8192];
    while (is.available() > 0) {
      int readBytes = is.read(buffer);
      os.write(buffer, 0, readBytes);
    }
  }

}
