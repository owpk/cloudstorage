package org.owpk.controller;

import javafx.application.Platform;
import javafx.scene.control.TableCell;
import org.owpk.util.FileInfo;

/**
 * Расчитывет единицы измерения размера файла {@link #computeSize(Long)}
 * создает ячейку в таблице
 */
public class SizeCellStyler extends TableCell<FileInfo, Long> {
  private final static String[] SIZES = {"B","KB","MB","GB","TB"};
  private int counter;

  @Override
  protected void updateItem(Long item, boolean empty) {
    super.updateItem(item, empty);
      if (item == null || empty) {
        setText(null);
        setStyle("");
      } else {
        String text;
        if (item == -1) text = "";
        else text = computeSize(item);
        setText(text);
      }
  }

  private String computeSize(Long bytes) {
    if (bytes > 1024) {
      bytes /= 1024;
      if (counter < SIZES.length)
        counter++;
      return computeSize(bytes);
    } else {
      String s = bytes + " " + SIZES[counter];
      counter = 0;
      return s;
    }
  }
}
