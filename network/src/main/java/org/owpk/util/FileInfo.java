package org.owpk.util;

import lombok.Data;
import org.apache.tika.Tika;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Describing origin file
 */
@Data
public class FileInfo implements Serializable {

  /**
   * File types
   */
  public enum FileType implements Serializable {
    EXEC("icons/exe_ico.png","exe"),
    ARCH("icons/archive.png", "arch"),
    IMAGE("icons/img_ico.png","image"),
    VID("icons/vid_ico.png", "video"),
    FILE("icons/file_ico.png","default"),
    HDD("icons/hard-disk.png", "hard_drive"),
    DIRECTORY("icons/folder_ico.png", "folder");

    private final String url;
    private final String type;
    FileType(String url, String type) {
      this.type = type;
      this.url = url;
    }
    public String getType() {
      return type;
    }
    public String getUrl() {
      return url;
    }
  }

  private String filename;
  private transient Path path;
  private FileType fileType;
  private Long size;
  private LocalDateTime lastModified;

  public FileInfo(Path path) {
    this.filename = path.getFileName().toString();
    this.path = path;
    fileType = Files.isDirectory(path) ? FileType.DIRECTORY : parseType(path);
    try {
      size = fileType == FileType.DIRECTORY ? -1L : (Files.size(path));
      lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * MIME parser {@link Tika},
   * @return {@code FileInfo.FileType}
   */
  public static FileInfo.FileType parseType(Path path) {
    Tika tika = new Tika();
    String mimeType = tika.detect(path.getFileName().toString());
    if (mimeType.startsWith("application")) {
      mimeType = applicationTypeParse(mimeType);
    } else mimeType = mimeType.substring(0, mimeType.indexOf("/")).trim();
    final EnumMap<FileType, String> MIME_TYPE_MAP = new EnumMap<>(FileType.class);
    Arrays.stream(FileType.values())
        .forEach(x -> MIME_TYPE_MAP.put(x, x.type));
    String finalMimeType = mimeType;
    return MIME_TYPE_MAP.entrySet().stream()
        .filter(x -> x.getValue().equals(finalMimeType))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(FileType.FILE);
  }

  private static String applicationTypeParse(String app) {
    app = app.substring(app.indexOf("/") + 1).trim();
    if (app.startsWith("x-rar") || app.startsWith("zip"))
      return FileType.ARCH.type;
    else if (app.equals("x-dosexec"))
      return FileType.EXEC.type;
    else return app;
  }

}
