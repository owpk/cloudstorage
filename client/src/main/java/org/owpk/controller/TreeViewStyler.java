package org.owpk.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import org.owpk.app.Callback;
import org.owpk.util.FileInfo;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Добавляет иконки к элементам {@link TreeView}
 */
public class TreeViewStyler {
  private static final ExecutorService es = Executors.newFixedThreadPool(5);
  private static Callback<String> textFlowCallBack;
  public static void setTextFlowCallBack(Callback<String> textFlowCallBack) {
    TreeViewStyler.textFlowCallBack = textFlowCallBack;
    System.out.println(textFlowCallBack);
  }

  public static void setupTreeView(TreeView<String> treeView) {
    treeView.setRoot(getNodesForDirectory(
        new File(FileSystems.getDefault().getRootDirectories().iterator().next().toString())));
    treeView.setOnMouseClicked(x -> {
      if (x.getClickCount() == 2 && x.getButton() == MouseButton.PRIMARY) {
        TreeItem<String> f = treeView.getSelectionModel().getSelectedItem();
        textFlowCallBack.call(f.getValue());
      }
    });
  }

  private static ImageView getIco(File f) {
    Image img = FileInfo.getIconMap().get(FileInfo.parseType(f.toPath()));
    return getImageView(img);
  }

  private static ImageView getImageView(Image img) {
    ImageView icon = new ImageView(img);
    icon.setFitWidth(17);
    icon.setFitHeight(17);
    icon.setPreserveRatio(true);
    return icon;
  }
  /**
   * Рекурсивно добавляет рут элементы друг к другу
   * создавая при этом новый поток, число потоков ограничено {@link ExecutorService}
   */
  private static TreeItem<String> getNodesForDirectory(File directory) {
    TreeItem<String> root = new TreeItem<>(directory.getName());
      File[] ff = directory.listFiles();
      if (ff != null) {
        Platform.runLater(() -> es.execute(() -> {
          for (File f : ff) {
            if (!f.isHidden()) {
              if (f.isDirectory() && f.canRead()) {
                root.getChildren().add(getNodesForDirectory(f));
                Platform.runLater(() -> root.setGraphic(
                    getImageView(FileInfo.getIconMap().get(FileInfo.FileType.DIRECTORY))));
              } else {
                root.getChildren().add(new TreeItem<>(f.getName(), getIco(f)));
              }
            }
          }
        }));
      }
    Platform.runLater(() -> root.setGraphic(
        getImageView(FileInfo.getIconMap().get(FileInfo.FileType.DIRECTORY))));
    return root;
  }
}
