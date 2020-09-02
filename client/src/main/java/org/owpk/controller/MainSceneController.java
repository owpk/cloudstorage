package org.owpk.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.owpk.app.ClientConfig;
import org.owpk.util.Config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Main controller class
 * Contains client panel, cloud panel and directory tree
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
  private ClientConfig config;
  private Stage stage;

  private double xOffset = 0;
  private double yOffset = 0;

  public void setStatusLabel(String text) {
    Platform.runLater(() -> status_label.setText(text));
  }

  private boolean draggable = true;

  public Stage getStage() {
    return stage;
  }

  /**
   * Initialization of the resize helper, window control buttons
   * and drag options for the top MenuBar element
   */
  public void initWindowControls(Stage stage) {
    this.stage = stage;
    ResizeHelper.addResizeListener(stage);

    //кнопка закрыть
    shut_down_btn.setOnMouseClicked(event -> {
      if (cloudPanelController.getNetworkServiceInt() != null)
        cloudPanelController.disconnect();
      config.writeProperty(Config.ConfigParameters.LAST_DIR, clientPanelController.getHistory().peek().toString());
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
    final FXMLLoader loader = new FXMLLoader(MainSceneController.class.getResource("/view/client_panel.fxml"));
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

  public ClientConfig getConfig() {
    return config;
  }

  public ClientPanelController getClientPanelController() {
    return clientPanelController;
  }

  public ClientPanelController getRightTabClientController() {
    return rightTabClientController;
  }

  public CloudPanelController getCloudPanelController() {
    return cloudPanelController;
  }

  @SneakyThrows
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fillElements();
    config = ClientConfig.getConfig();

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
