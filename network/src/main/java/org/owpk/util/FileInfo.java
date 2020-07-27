package org.owpk.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Data;
import org.apache.tika.Tika;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Data
public class FileInfo {

  public enum FileType {
    EXEC("icons/exe_ico.png","exe"),
    ARCH("icons/ar_ico.jpg", "arch"),
    IMAGE("icons/img_ico.png","image"),
    VID("icons/vid_ico.png", "video"),
    FILE("icons/file_ico.png","default"),
    DIRECTORY("icons/folder_ico.png", "folder");

    private final String url;
    private final String mimeType;
    FileType(String url, String mimeType) {
      this.mimeType = mimeType;
      this.url = url;
    }
    public String getMimeType() {
      return mimeType;
    }
    public String getUrl() {
      return url;
    }

  }

  private String filename;
  private FileType fileType;
  private Long size;
  private LocalDateTime lastModified;
  private ObjectProperty<FileType> imageType;

  public FileInfo(Path path) {
    this.filename = path.getFileName().toString();
    fileType = Files.isDirectory(path) ? FileType.DIRECTORY : parseType(path);
    this.imageType = new SimpleObjectProperty<>(fileType);
    try {
      size = fileType == FileType.DIRECTORY ? -1L : (Files.size(path));
      lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private FileInfo.FileType parseType(Path path) {
    Tika tika = new Tika();
    String mimeType = tika.detect(path.getFileName().toString());
    if (mimeType.startsWith("application")) {
      mimeType = applicationTypeParse(mimeType);
    } else mimeType = mimeType.substring(0, mimeType.indexOf("/")).trim();;
    System.out.println(path.getFileName() + " : " + mimeType);

    final EnumMap<FileType, String> mimeTypeMap = new EnumMap<>(FileType.class);
    Arrays.stream(FileType.values())
        .forEach(x -> mimeTypeMap.put(x, x.mimeType));
    String finalMimeType = mimeType;
    return mimeTypeMap.entrySet().stream()
        .filter(x -> x.getValue().equals(finalMimeType))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(FileType.FILE);
  }

  private String applicationTypeParse(String app) {
    System.out.println(app);
    app = app.substring(app.indexOf("/") + 1).trim();
    if (app.startsWith("x-rar") || app.startsWith("zip"))
      return FileType.ARCH.mimeType;
    else if (app.equals("x-dosexec"))
      return FileType.EXEC.mimeType;
    else return app;
  }

}
