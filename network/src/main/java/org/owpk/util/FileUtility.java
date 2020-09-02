package org.owpk.util;

import org.owpk.message.DataInfo;
import org.owpk.message.Message;
import org.owpk.message.MessageType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public static File createDirectory(String dirName) throws IOException {
    File file = new File(dirName);
    if (!file.exists()) {
      file.mkdir();
    }
    return file;
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

  public static void deleteFile(Path path) throws IOException {
    Files.deleteIfExists(path);
  }

  @SafeVarargs
  public static void sendFileByChunks(OutputCallback<Message<?>> out, File f, MessageType type, Callback<Float>... callbacks) throws IOException {
    int chunkCount;
    try (FileInputStream fis = new FileInputStream(f)) {
      byte[] buf = new byte[BUFFER_SIZE];
      chunkCount = (int) Math.ceil((float) f.length() / BUFFER_SIZE);
      int chunkIndex = 0;
      while (fis.available() > 0) {
        int offset = fis.read(buf);
        byte[] chunk = Arrays.copyOf(buf, offset);
        out.call(new DataInfo(type, chunkCount, chunkIndex, f.getName(), chunk));
        if (callbacks.length > 0) {
          float counter;
          counter = (float) chunkIndex / chunkCount;
          callbacks[0].call(counter);
        }
        chunkIndex++;
      }
    }
  }

  /**
   * Класс принимает пакет, находит нужный FileWriter отностительно имени файла, и записывает массив байт в файл
   */
  public static class FileWriter {
    private final FileOutputStream fos;
    private static final Map<String, FileWriter> writerMap = new HashMap<>();
    private final String fileName;

    public FileWriter(String fileName) throws FileNotFoundException {
      this.fileName = fileName;
      fos = new FileOutputStream(fileName, true);
    }

    public static FileWriter getWriter(String fileName) throws FileNotFoundException {
      FileWriter writer = writerMap.get(fileName);
      if (writer == null) {
        writer = new FileWriter(fileName);
        writerMap.put(fileName, writer);
      }
      return writer;
    }

    public void assembleChunkedFile(DataInfo ms) throws IOException {
      //TODO проверка индекса пакета и временная запись в буфер или выбрасывание ошибки
      byte[] payload = ms.getPayload();
      int index = ms.getChunkIndex();
      fos.write(payload);
      if (index == ms.getChunkCount() - 1) {
        writerMap.remove(fileName);
        fos.close();
        System.out.println("DONE");
      }
    }
  }

}
