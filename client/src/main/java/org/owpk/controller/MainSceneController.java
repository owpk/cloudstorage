package org.owpk.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.owpk.app.Config;
import org.owpk.util.FileInfo;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * основной контроллер
 */
public class MainSceneController implements Initializable {
  @FXML private MenuBar drag_menu;
  @FXML private Button shut_down_btn;
  @FXML private Button collapse_btn;
  @FXML private Button expand_btn;
  @FXML private Label status_label;
  @FXML private VBox main_window;
  @FXML private VBox tree_window;
  @FXML private TreeView<String> tree_view;
  @FXML private VBox left_panel_view;
  @FXML private VBox right_local_panel_view;
  @FXML private VBox right_cloud_panel_view;

  private ClientPanelController clientPanelController;
  private ClientPanelController rightTabClientController;
  private CloudPanelController cloudPanelController;

  private Stage stage;

  private double xOffset = 0;
  private double yOffset = 0;

  private static final Map<FileInfo.FileType, Image> ICON_MAP;

  static {
    ICON_MAP = new HashMap<>();
    Arrays.stream(FileInfo.FileType.values())
        .forEach(x -> {
          System.out.println(x.getUrl());
          ICON_MAP.put(x, new Image(x.getUrl()));
        });
  }

  public void setStatusLabel(String text) {
    Platform.runLater(() -> status_label.setText(text));
  }

  public static Map<FileInfo.FileType, Image> getIconMap() {
    return ICON_MAP;
  }

  private boolean draggable = true;

  public Stage getStage() {
    return stage;
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

    drag_menu.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        stage.setFullScreen(!stage.isFullScreen());
      }
    });

    //делаем окно draggable
    drag_menu.setOnMousePressed(event -> {
      if (event.getY() < 5)
        draggable = false;
      else {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
        draggable = true;
      }
    });
    drag_menu.setOnMouseDragged(event -> {
      if (draggable) {
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
      }
    });
  }

  private Parent getClientView() throws IOException {
    final FXMLLoader loader = new FXMLLoader(MainSceneController.class.getResource("/client_panel.fxml"));
    Parent parent = loader.load();
    ClientPanelController clientPanelController = loader.getController();
    clientPanelController.init();
    clientPanelController.setMainSceneController(this);
    return parent;
  }

  @FXML
  private void switchToLocal() throws IOException { }

  @FXML
  private void switchToCloud() throws IOException { }

  public void refreshAllClientPanels() {
    clientPanelController.clientRefresh();
    rightTabClientController.clientRefresh();
  }

  private void fillElements() {
    Platform.runLater(() -> TreeViewController.setupTreeView(tree_view));
  }

  @SneakyThrows
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fillElements();

    rightTabClientController = (ClientPanelController) right_local_panel_view.getProperties().get("ctrl");
    rightTabClientController.setMainSceneController(this);
    rightTabClientController.init();

    clientPanelController = (ClientPanelController) left_panel_view.getProperties().get("ctrl");
    clientPanelController.setMainSceneController(this);
    clientPanelController.init();

    cloudPanelController = (CloudPanelController) right_cloud_panel_view.getProperties().get("cloud");
    cloudPanelController.setMainSceneController(this);
    cloudPanelController.init();
  }

}
