package org.owpk.controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.owpk.util.FileInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IconBuilder {
  private final static Map<String, Image> ICON_MAP;
  private static final Map<FileInfo.FileType, Image> F_TYPE_ICON_MAP;
  static {
    ICON_MAP = new HashMap<>();
    F_TYPE_ICON_MAP = new HashMap<>();
    Arrays.stream(FileInfo.FileType.values())
        .forEach(x -> {
          System.out.println(x.getUrl());
          F_TYPE_ICON_MAP.put(x, new Image(x.getUrl()));
        });
    ICON_MAP.put("download", new Image("icons/dwnld_ico.png"));
  }

  private final ImageView imageView;

  public IconBuilder() {
    imageView = new ImageView();
    imageView.setFitWidth(20);
    imageView.setFitHeight(20);
    imageView.setSmooth(true);
    imageView.setPreserveRatio(true);
  }

  public ImageView build() {
    return imageView;
  }

  public IconBuilder setFitWidth(int width) {
    imageView.setFitWidth(width);
    return this;
  }

  public IconBuilder setFitHeight(int height) {
    imageView.setFitHeight(height);
    return this;
  }

  public IconBuilder setIconImage(String image) {
    imageView.setImage(ICON_MAP.get(image));
    return this;
  }

  public IconBuilder setIconImage(FileInfo.FileType type) {
    imageView.setImage(F_TYPE_ICON_MAP.get(type));
    return this;
  }

  public static Map<String, Image> getIconMap() {
    return ICON_MAP;
  }

  public static Map<FileInfo.FileType, Image> getFileTypeIconMap() {
    return F_TYPE_ICON_MAP;
  }
}
