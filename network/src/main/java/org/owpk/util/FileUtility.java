package org.owpk.util;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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

  public static List<FileInfo> showDirs(String currentDir) throws IOException {
    Path path = Paths.get(currentDir);
    return Files.list(path).map(FileInfo::new)
        .collect(Collectors.toList());
  }

  @Deprecated
  public static void move(File dir, File file) throws IOException {
    String path = dir.getAbsolutePath() + "/" + file.getName();
    createFile(path);
    InputStream is = new FileInputStream(file);
    try (OutputStream os = new FileOutputStream(new File(path))) {
      byte[] buffer = new byte[8192];
      while (is.available() > 0) {
        int readBytes = is.read(buffer);
        os.write(buffer, 0, readBytes);
      }
    }
  }

  public static void move(Path source, Path target) throws IOException {
    if (Files.isDirectory(source)) {
      String targetDirName = source.getFileName().toString();
      Files.createDirectory(Paths.get(target.toString(), targetDirName));
      File[] files = source.toFile().listFiles();
      if (files != null && files.length != 0) {
        for (File f : files)
          move(f.toPath(), Paths.get(target.toString(), targetDirName));
      }
      Files.deleteIfExists(source);
    } else {
      Files.move(source, Paths.get(
          target.toString(), source.getFileName().toString()), REPLACE_EXISTING);
    }
  }

  public static void placeFile(DataInputStream is, File file) throws IOException {
    byte[] buffer = new byte[8192];
    try (FileOutputStream fos = new FileOutputStream(file)) {
      for (long i = 0; i < buffer.length; i++) {
        int count = is.read(buffer);
        fos.write(buffer, 0, count);
      }
    }
  }

  public static void sendFile(DataOutputStream os, File file) throws IOException {
    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[8192];
      while (fis.available() > 0) {
        int count = fis.read(buffer);
        os.write(buffer, 0, count);
      }
    }
  }

}
