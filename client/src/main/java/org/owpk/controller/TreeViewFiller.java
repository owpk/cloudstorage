package org.owpk.controller;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.owpk.app.Callback;
import org.owpk.util.FileInfo;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Стилизатор и филлер для дерева каталогов {@link TreeView}
 * Создает дерево каталогов и добавляет иконки к каждому элементу в зависмости от типа
 *
 * @see org.owpk.util.FileInfo
 */
public class TreeViewFiller {
  private static Callback<String> textFlowCallBack;
  private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
  private static final String ROOT_NODE_NAME = "Local File System";
  private static final int DEPTH = 2;
  private static AtomicInteger recursionDepth = new AtomicInteger();
  static {
     //значение больше 2 сильно снижает производительность
    recursionDepth.set(DEPTH);
  }

  public static void setTextFlowCallBack(Callback<String> textFlowCallBack) {
    TreeViewFiller.textFlowCallBack = textFlowCallBack;
  }

  public static void shutDownExecutorService() {
    executorService.shutdownNow();
  }

  /**
   * Детектит имена всех дисков на локальной машине, заполняет дерево каталогов
   * по событию Expand пересчитывает путь
   * @see #getNodesForDirectory(File[], String)
   */
  //TODO переписать обновление директорий по событию клика
  public static void setupTreeView(TreeView<String> treeView) {
    final Object lock = new Object();
    TreeItem<String> treeItemRoot = new TreeItem<>(ROOT_NODE_NAME);
    treeItemRoot.addEventHandler(EventType.ROOT, event -> {
      if (event.getEventType().getName().equals("BranchExpandedEvent")) {
        recursionDepth.incrementAndGet();
        TreeItem<String> item = (TreeItem<String>) event.getSource();
        item.getChildren().clear();
        File[] dirs = new File(getPath(item)).listFiles();
        if (dirs != null) {
          for (File f : dirs) {
            if (!f.isHidden() && f.isDirectory()) {
              executorService.submit(() -> {
                item.getChildren().add(getNodesForDirectory(dirs, f.getName()));
              });
            }
          }
        }
      } else if (event.getEventType().getName().equals("BranchCollapsedEvent")) {
        TreeItem<String> item = (TreeItem<String>) event.getSource();
        recursionDepth.set(rootCounter(item) + DEPTH);
      }
    });

    Arrays.stream(File.listRoots())
        .filter(x -> Objects.nonNull(x) && x.length() > 0)
        .forEach(x ->
            executorService.execute(() -> {
              synchronized (lock) {
                TreeItem<String> item = new TreeItem<>(x.getAbsolutePath());
                item.setGraphic(getImageView(FileInfo.getIconMap().get(FileInfo.FileType.HDD)));
                item.getChildren().add(getNodesForDirectory(x.listFiles(), x.getAbsolutePath()));
                treeItemRoot.getChildren().add(item);
              }
            }));
    treeItemRoot.setExpanded(true);
    treeView.setRoot(treeItemRoot);
    treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    treeView.setOnMouseClicked(x -> {

      TreeItem<String> f = treeView.getSelectionModel().getSelectedItem();
      if (f != null && f.getValue() != null && !f.getValue().isEmpty()) {
        String p = getPath(f);
        final File ff = new File(p);
        System.out.println(p);
        if (ff.exists() && ff.isDirectory()) {
          textFlowCallBack.call(p);
        }
      }
    });
  }

  /**
   * аппендер имени директорий по событию клика
   */
  private static StringBuffer sb = new StringBuffer();
  private static String getPath(TreeItem<String> item) {
    if (item == null || item.getValue().isEmpty() || item.getValue().equals(ROOT_NODE_NAME)) {
      String res = sb.toString();
      sb = new StringBuffer();
      return res;
    } else {
      sb.insert(0, item.getValue() + "\\");
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
    icon.setSmooth(true);
    return icon;
  }

  /**
   * Рекурсивно добавляет рут элементы друг к другу
   * глубина рекурсии ограничена {@link #DEPTH}
   * по клику на развертывание ветки активируется ее обновление на установленную глубину
   */
  private static TreeItem<String> getNodesForDirectory(File[] directory, String dirName) {
    TreeItem<String> root = new TreeItem<>(dirName);
    Platform.runLater(() -> root.setGraphic(
        getImageView(FileInfo.getIconMap().get(FileInfo.FileType.DIRECTORY))));
    if (directory != null) {
        for (File f : directory) {
          if (!f.isHidden()) {
            if (f.isDirectory() && f.canRead()) {
              Platform.runLater(() -> {
                if (rootCounter(root) < recursionDepth.get()) {
                  root.getChildren().add(getNodesForDirectory(f.listFiles(), f.getName()));
                }
              });
          }
        }
      }
    }
    return root;
  }

  private static int rootCounter(TreeItem<String> item) {
    int counter = 0;
    item = item.getParent();
    while (item != null) {
      counter++;
      item = item.getParent();
    }
    return counter;
  }

}
