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
 * Стилизатор и филлер для дерева каталогов {@link TreeView}
 * Создает дерево каталогов и добавляет иконки к каждому элементу в зависмости от типа
 *
 * @see org.owpk.util.FileInfo
 */
public class TreeViewFiller {
  private static Callback<String> textFlowCallBack;
  private static final String ROOT_NODE_NAME = "Local File System";
  private static TreeView<String> baseTree;
  private static TreeItem<String> rootItem;

  public static void setTextFlowCallBack(Callback<String> textFlowCallBack) {
    TreeViewFiller.textFlowCallBack = textFlowCallBack;
  }

  public static void setupTreeView(TreeView<String> treeView) {
    TreeViewFiller.baseTree = treeView;
    rootItem = new TreeItem<>(ROOT_NODE_NAME);
    rootItem.setExpanded(true);
    setupListeners();
    fillTreeItems();
  }

  private static void fillTreeItems() {
    Arrays.stream(File.listRoots())
        .filter(x -> Objects.nonNull(x) && x.length() > 0)
        .forEach(x ->
        {
          TreeItem<String> item = new TreeItem<>(x.getAbsolutePath());
          item.setGraphic(getImageView(FileInfo.getIconMap().get(FileInfo.FileType.HDD)));
          if (checkIfDirectoriesExists(x.listFiles())) {
            item.getChildren().add(new TreeItem<>());
          }
          rootItem.getChildren().add(item);

        });
    baseTree.setRoot(rootItem);
    baseTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  private static void setupListeners() {
    rootItem.addEventHandler(EventType.ROOT, event -> {
      if (event.getEventType().getName().equals("BranchExpandedEvent")) {
        TreeItem<String> item = (TreeItem<String>) event.getSource();
        item.getChildren().clear();
        Thread t = new Thread(() -> {
          populateItem(item);
          for (TreeItem<String> child : item.getChildren()) {
            populateItem(child);
          }
        });
        t.setDaemon(true);
        t.start();
      }
    });
    baseTree.setOnMouseClicked(x -> {
      TreeItem<String> f = baseTree.getSelectionModel().getSelectedItem();
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
   * аппендер имени директории по событию клика
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

  private static ImageView getImageView(Image img) {
    ImageView imageView = new ImageView(img);
    imageView.setFitWidth(17);
    imageView.setFitHeight(17);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);
    return imageView;
  }

  private static void populateItem(TreeItem<String> item) {
    File[] dirs = new File(getPath(item)).listFiles();
    if (dirs != null) {
      for (File f : dirs) {
        if (checkFileCondition(f)) {
          TreeItem<String> child = new TreeItem<>(f.getName());
          Platform.runLater(() ->
              child.setGraphic(getImageView(FileInfo.getIconMap().get(FileInfo.FileType.DIRECTORY))));
          item.getChildren().add(child);
        }
      }
    }
  }

  private static boolean checkFileCondition(File f) {
    return f != null && f.exists() && !f.isHidden()
        && f.isDirectory() && f.canRead() && f.listFiles() != null;
  }

  private static boolean checkIfDirectoriesExists(File[] f) {
    return f != null && Arrays.stream(f)
        .parallel()
        .anyMatch(TreeViewFiller::checkFileCondition);
  }

}
