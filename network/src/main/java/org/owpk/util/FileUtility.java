package org.owpk.util;

import org.owpk.message.DataInfo;
import org.owpk.message.MessageType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtility {
  private static final int BUFFER_SIZE = 8192;

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

  public static List<FileInfo> getDirectories(String currentDir) throws IOException {
    Path path = Paths.get(currentDir);
    return Files.list(path).map(FileInfo::new)
        .collect(Collectors.toList());
  }

  //перемещает всю директорию и файлы в ней или отдельный файл
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

  public static DataInfo[] getChunkedFile(File f, MessageType type) throws IOException {
    DataInfo[] buffer;
    int chunkCount;
    try(FileInputStream fis = new FileInputStream(f)) {
      byte[] buf = new byte[BUFFER_SIZE];
      chunkCount = (int) Math.ceil((float) f.length() / BUFFER_SIZE);
      System.out.println("Chunks : " + chunkCount + " Size: " + f.length());
      buffer = new DataInfo[chunkCount ];
      int chunkIndex = 0;
      while (fis.available() > 0) {
        int offset = fis.read(buf);
        byte[] chunk = Arrays.copyOf(buf, offset);
        buffer[chunkIndex] = new DataInfo(type, chunkCount, chunkIndex, f.getName(), chunk);
        chunkIndex++;
      }
    }
    return buffer;
  }

}
