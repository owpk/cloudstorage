package org.owpk.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.owpk.app.Config;
import org.owpk.util.FileInfo;

import java.net.URL;
import java.util.*;

/**
 * основной контроллер
 */
public class MainSceneController implements Initializable {
  @FXML public MenuBar drag_menu;
  @FXML public Button shut_down_btn;
  @FXML public Button collapse_btn;
  @FXML public Button expand_btn;
  @FXML public Label status_label;
  @FXML public VBox main_window;
  @FXML public VBox tree_window;
  @FXML public TreeView<String> tree_view;
  @FXML public VBox client_panel_view;
  @FXML public VBox cloud_panel_view;

  private ClientPanelController clientPanelController;
  private CloudPanelController cloudPanelController;

  private Stage stage;

  private double xOffset = 0;
  private double yOffset = 0;

  private static final Map<FileInfo.FileType, Image> iconMap;
  static {
    iconMap = new HashMap<>();
    Arrays.stream(FileInfo.FileType.values())
        .forEach(x -> {
          System.out.println(x.getUrl());
          iconMap.put(x, new Image(x.getUrl()));
        });
  }
  public static Map<FileInfo.FileType, Image> getIconMap() {
    return iconMap;
  }

  /**
   * инициализация ресайзера, кнопок управления окном
   * и драг опции для верхнего MenuBar элемента
   */
  public void initWindowControls(Stage stage) {
    this.stage = stage;
    ResizeHelper.addResizeListener(stage);

    //кнопка закрыть
    shut_down_btn.setOnMouseClicked(event -> {
      Config.setSourceRoot(clientPanelController.clientBackInHistoryStack.peek().toString());
      Platform.exit();
    });
    //кнопка фул скрин/базовый размер
    collapse_btn.setOnMouseClicked(event -> stage.setFullScreen(!stage.isFullScreen()));
    //кнопка минимайз
    expand_btn.setOnAction(e -> ((Stage) ((Button) e.getSource())
        .getScene()
        .getWindow())
        .setIconified(true));
    //делаем окно draggable
    drag_menu.setOnMousePressed(event -> {
       xOffset = event.getSceneX();
       yOffset = event.getSceneY();
    });
    drag_menu.setOnMouseDragged(event -> {
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    });
  }

  private void fillElements() {
    Platform.runLater(() -> TreeViewController.setupTreeView(tree_view));
  }

  @SneakyThrows
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fillElements();
    clientPanelController = (ClientPanelController) client_panel_view.getProperties().get("ctrl");
    cloudPanelController = (CloudPanelController) cloud_panel_view.getProperties().get("cloud");
    clientPanelController.setMainSceneController(this);
    cloudPanelController.setMainSceneController(this);
    clientPanelController.init();
    cloudPanelController.init();
  }
}
