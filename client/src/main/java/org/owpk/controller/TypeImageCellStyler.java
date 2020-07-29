package org.owpk.controller;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.owpk.util.FileInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Создает иконку для ячейки в таблице в зависимости от типа файла
 */
public class TypeImageCellStyler<T> extends TableCell<T, FileInfo.FileType> {
  private final ImageView image;
  private static final Map<FileInfo.FileType, Image> iconMap = FileInfo.getIconMap();

  public TypeImageCellStyler() {
    image = new ImageView();
    image.setFitWidth(20);
    image.setFitHeight(20);
    image.setPreserveRatio(true);
    setGraphic(image);
    setMinHeight(20);
  }

  @Override
  protected void updateItem(FileInfo.FileType type, boolean empty) {
    super.updateItem(type, empty);
    if (empty || type == null)
      image.setImage(null);
    else {
      image.setImage(iconMap.get(type));
    }
  }
}
