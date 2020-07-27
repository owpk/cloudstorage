package org.owpk.controller;

import javafx.scene.control.TableCell;
import org.owpk.util.FileInfo;

public class SizeCell extends TableCell<FileInfo, Long> {
  final static String[] SIZES = {"b","Kb","Mb","Gb","Tb"};
  private static int counter;

  @Override
  protected void updateItem(Long item, boolean empty) {
    super.updateItem(item, empty);
    if (item == null || empty) {
      setText(null);
      setStyle("");
    } else {
      String text;
      if (item == -1) text = "dir";
      else text = parseSize(item);
      setText(text);
    }
  }

  private String parseSize(Long res) {
    if (res > 1024) {
      res /= 1024;
      if (counter < SIZES.length)
        counter++;
      return parseSize(res);
    } else {
      String result = res + " " + SIZES[counter];
      counter = 0;
      return result;
    }
  }
}
