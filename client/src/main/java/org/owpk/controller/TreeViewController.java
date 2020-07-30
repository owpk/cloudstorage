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

/**
 * Контроллер {@link TreeView} слушает события "BranchExpandedEvent" {@link #setupListeners()},
 * по событию вычисляет абсолютный путь и добавляет к текущему TreeItem элементу на котором произошел вызов
 * все папки которые удалось найти по этому пути {@link #populateItem(TreeItem)},
 * в свою очередь в каждой из этих папок проверяется наличие хотябы одной папки, если проверка пройдена,
 * добавялется пустой узел заглушка для возможности вызова события "BranchExpandedEvent"
 */
public class TreeViewController {
  private static Callback<String> textFlowCallBack;
  private static final String ROOT_NODE_NAME = "Local File System";
  private static TreeView<String> treeView;
  private static TreeItem<String> rootItem;

  public static void setTextFlowCallBack(Callback<String> textFlowCallBack) {
    TreeViewController.textFlowCallBack = textFlowCallBack;
  }

  public static void setupTreeView(TreeView<String> treeView) {
    TreeViewController.treeView = treeView;
    rootItem = new TreeItem<>(ROOT_NODE_NAME);
    rootItem.setExpanded(true);
    setupListeners();
    fillTreeItems();
  }

  /**
   * вызывается при первом запуске, доавляет TreeItem с названием дисков
   */
  private static void fillTreeItems() {
    Arrays.stream(File.listRoots())
        .filter(x -> Objects.nonNull(x) && x.length() > 0)
        .forEach(x ->
        {
          TreeItem<String> item = new TreeItem<>(x.getAbsolutePath());
          item.setGraphic(getImageView(MainSceneController.getIconMap().get(FileInfo.FileType.HDD)));
          expanded(item);
          item.setExpanded(true);
          rootItem.getChildren().add(item);

        });
    treeView.setRoot(rootItem);
    treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  private static void expanded(TreeItem<String> item) {
    Thread t = new Thread(() -> {
      populateItem(item);
      for (TreeItem<String> child : item.getChildren()) {
        populateItem(child);
      }
    });
    t.setDaemon(true);
    t.start();
  }

  /**
   * инициализирует слушателей на события BranchExpandedEvent и OnMouseClicked
   */
  private static void setupListeners() {
    rootItem.addEventHandler(EventType.ROOT, event -> {
      if (event.getEventType().getName().equals("BranchExpandedEvent")) {
        TreeItem<String> item = (TreeItem<String>) event.getSource();
        item.getChildren().clear();
        expanded(item);
      }
    });
    treeView.setOnMouseClicked(x -> {
      TreeItem<String> f = treeView.getSelectionModel().getSelectedItem();
      if (f != null && f.getValue() != null && !f.getValue().isEmpty()) {
        String p = getPath(f);
        final File ff = new File(p);
        if (ff.exists() && ff.isDirectory()) {
          textFlowCallBack.call(p);
        }
      }
    });
  }

  /**
   * Вычесляет абсолютный путь, собирает имена всех Parent узлов у TreeItem
   */
  private static StringBuffer sb = new StringBuffer();
  private static String getPath(TreeItem<String> item) {
//    item = item.getParent();
//    while (item.getValue() != ROOT_NODE_NAME) {
//
//    }
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

  private static void populateItem(TreeItem<String> item) {
    File[] dirs = new File(getPath(item)).listFiles();
    if (dirs != null) {
      for (File f : dirs) {
        if (checkFileCondition(f)) {
          TreeItem<String> child = new TreeItem<>(f.getName());
          Platform.runLater(() ->
              child.setGraphic(getImageView(MainSceneController.getIconMap().get(FileInfo.FileType.DIRECTORY))));
          item.getChildren().add(child);
        }
      }
    }
  }

  private static ImageView getImageView(Image img) {
    ImageView imageView = new ImageView(img);
    imageView.setFitWidth(17);
    imageView.setFitHeight(17);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    return imageView;
  }

  private static boolean checkFileCondition(File f) {
    return f != null && f.exists() && !f.isHidden()
        && f.isDirectory() && f.canRead() && f.listFiles() != null;
  }

  private static boolean checkIfDirectoriesExists(File[] f) {
    return f != null && Arrays.stream(f)
        .parallel()
        .anyMatch(TreeViewController::checkFileCondition);
  }

}
