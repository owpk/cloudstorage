package org.owpk.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import org.owpk.app.Callback;
import org.owpk.util.FileInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Стилизатор и филлер для дерева каталогов {@link TreeView}
 * Создает дерево каталогов и добавляет иконки к каждому элементу в зависмости от типа
 * @see org.owpk.util.FileInfo
 */
public class TreeViewStylist {
  private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
  private static Callback<String> textFlowCallBack;
  private static final String ROOT_NODE_NAME = "Local File System";
  public static void setTextFlowCallBack(Callback<String> textFlowCallBack) {
    TreeViewStylist.textFlowCallBack = textFlowCallBack;
  }

  /**
   * Детектит имена всех дисков на локальной машине, заполняет дерево каталогов
   * @see #getNodesForDirectory(File, String)
   */
  public static void setupTreeView(TreeView<String> treeView) {
    final Object lock = new Object();
    TreeItem<String> treeItemRoot = new TreeItem<>(ROOT_NODE_NAME);
    Arrays.stream(File.listRoots())
        .parallel() //check
        .filter(x -> Objects.nonNull(x) && x.length() > 0)
        .forEach(x ->
            executorService.execute(() -> {
                synchronized (lock) {
                  treeItemRoot.getChildren()
                      .add(getNodesForDirectory(x.getAbsoluteFile(), x.getAbsolutePath()));
                }
            }));
    treeItemRoot.setExpanded(true);
    treeView.setRoot(treeItemRoot);
    treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    treeView.setOnMouseClicked(x -> {

        TreeItem<String> f = treeView.getSelectionModel().getSelectedItem();
        if (f != null && f.getValue() != null && !f.getValue().isEmpty()) {
          String p = getPath(f).substring(ROOT_NODE_NAME.length() + 2);
          final File ff = new File(p);
          if (ff.exists() && ff.isDirectory()) {
            textFlowCallBack.call(p);
          
        }
      }
    });
  }

  private static StringBuffer sb = new StringBuffer();
  /**
   * рекурсивно аппендит имена директорий по событию клика
   */
  private static String getPath(TreeItem<String> item) {
    if (item == null || item.getValue().isEmpty()) {
      String res = sb.toString();
      sb = new StringBuffer();
      return res;
    } else {
      sb.insert(0, "\\"+item.getValue());
      item = item.getParent();
      return getPath(item);
    }
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
   * создавая при этом новый поток, число потоков ограничено {@link #executorService}
   */
  private static TreeItem<String> getNodesForDirectory(File directory, String dirName) {
    TreeItem<String> root = new TreeItem<>(dirName);
      File[] ff = directory.listFiles();
      if (ff != null) {
        executorService.execute(() -> {
          for (File f : ff) {
            if (!f.isHidden()) {
              if (f.isDirectory() && f.canRead()) {
                root.getChildren().add(getNodesForDirectory(f, f.getName()));
                Platform.runLater(() -> root.setGraphic(
                    getImageView(FileInfo.getIconMap().get(FileInfo.FileType.DIRECTORY))));
              } else {
                  root.getChildren().add(new TreeItem<>(f.getName(), getIco(f)));
              }
            }
          }
        });
      }
    Platform.runLater(() -> root.setGraphic(
        getImageView(FileInfo.getIconMap().get(FileInfo.FileType.DIRECTORY))));
    return root;
  }
}
