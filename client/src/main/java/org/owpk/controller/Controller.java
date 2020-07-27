package org.owpk.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.owpk.util.FileInfo;
import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.app.Callback;
import org.owpk.app.Config;
import org.owpk.app.NetworkHandlerFactory;
import org.owpk.app.NetworkServiceInt;
import org.owpk.network.InputDataHandler;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller implements Initializable {

  @FXML public MenuBar drag_menu;
  @FXML public Button shut_down_btn;
  @FXML public Button roll_down_btn;
  @FXML public Button roll_up_btn;
  @FXML public TableView<FileInfo> server_panel;
  @FXML public TableView<FileInfo> client_panel;
  @FXML public Button server_refresh_btn;
  @FXML public Button client_refresh_btn;
  @FXML public TextField server_textFlow;
  @FXML public TextField client_textFlow;
  private Callback<Stream<Path>> serverTableCallback;
  private NetworkServiceInt networkServiceInt;

  private double xOffset = 0;
  private double yOffset = 0;

  public void setStageAndSetupListeners(Stage stage) {
    //кнопка закрыть
    shut_down_btn.setOnMouseClicked(event -> { disconnect();
      stage.close(); });
    //кнопка фул скрин/базовый размер
    roll_down_btn.setOnMouseClicked(event -> stage.setFullScreen(!stage.isFullScreen()));
    //кнопка минимайз
    roll_up_btn.setOnAction(e -> ((Stage) ((Button) e.getSource())
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

  public void refreshClientFolders(ActionEvent actionEvent) throws IOException {
    clientRefresh(Paths.get(client_textFlow.getText()));
  }

  private void clientRefresh(Path p) throws IOException {
    client_panel.getItems().clear();
    client_panel.getItems().addAll(Files
        .list(p)
        .map(FileInfo::new)
        .collect(Collectors.toList()));
  }

  private void serverRefresh(Stream<Path> files) {
    server_panel.getItems().clear();
    server_panel.getItems().addAll(files
        .map(FileInfo::new)
        .collect(Collectors.toList()));
  }

  public void refreshServerFolders(ActionEvent actionEvent) throws IOException {
    String path = server_textFlow.getText();
    if (path == null)
      path = "";
    System.out.println(path);
    if (networkServiceInt == null)
    connect();
    ((ObjectOutputStream) networkServiceInt.getOut()).writeObject(new Messages<>(MessageType.DIR, path));
    System.out.println("-:get DIR cmd");
  }

  private void connect(/*ActionEvent actionEvent*/) throws IOException {
    try {
      networkServiceInt = NetworkHandlerFactory.getHandler("localhost");
      networkServiceInt.connect();
      InputDataHandler inputDataHandler = new InputDataHandler(networkServiceInt);
      networkServiceInt.onMessageReceived(inputDataHandler);
    } catch (IOException e) {
      System.out.println("-:connection error:");
    }
  }

  private void disconnect() {
    if (networkServiceInt != null) {
      try {
        networkServiceInt.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  @SneakyThrows
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    clientRefresh(Paths.get(Config.getSourceRoot()));
    serverTableCallback = s -> Platform.runLater(() -> serverRefresh(s));
  }
}
