package org.owpk.controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.owpk.app.Callback;
import org.owpk.app.Config;
import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.network.InputDataHandler;
import org.owpk.network.NetworkHandlerFactory;
import org.owpk.network.NetworkServiceInt;
import org.owpk.util.FileInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * основной контроллер
 */
public class MainSceneController implements Initializable {
  @FXML public MenuBar drag_menu;
  @FXML public Button shut_down_btn;
  @FXML public Button roll_down_btn;
  @FXML public Button roll_up_btn;
  @FXML public TableView<FileInfo> server_panel;
  @FXML public Button server_forward_btn;
  @FXML public TextField server_textFlow;
  @FXML public Label status_label;
  @FXML public Button connect_btn;
  @FXML public VBox main_window;
  @FXML public VBox tree_window;
  @FXML public TreeView<String> tree_view;
  @FXML public VBox client_panel_view;
  private ClientPanelController clientPanelController;

  private Callback<List<FileInfo>> serverTableCallback;
  private Callback<String> serverStatusLabel;

  private Callback<String> statusLabelCallback;
  private NetworkServiceInt networkServiceInt;

  private Stack<Path> serverPathHistory;
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
      disconnect();
      Platform.exit();
    });
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

  /**
   * метод вызывается при нажатии на кнопку "connect"
   * {@link NetworkHandlerFactory} возвращает {@link NetworkServiceInt},
   * который создает подключение клиента к серверу,
   * потоки данных для созданного подключения обслуживает {@link InputDataHandler}
   * команда выполняется в отедльном сервисном потоке, чтобы не мешать UI
   */
  public void connect(ActionEvent actionEvent) {
    Service<Void> ser = new Service<Void>() {
      @Override
      protected Task<Void> createTask() {
        return new Task<Void>() {
          @Override
          protected Void call() throws InterruptedException {
            try {
              networkServiceInt = NetworkHandlerFactory.getHandler(Config.getDefaultServer());
              networkServiceInt.connect();
              InputDataHandler inputDataHandler =
                  new InputDataHandler(networkServiceInt,
                      serverTableCallback, serverStatusLabel);
              forwardServerFolders(actionEvent);
              networkServiceInt.onMessageReceived(inputDataHandler);
            } catch (IOException e) {
              networkServiceInt = null;
              throw new InterruptedException();
            }
            return null;
          }
        };
      }
    };
    //выводим информацию в текс лейбл по результату выполнения
    ser.setOnRunning((WorkerStateEvent event) ->
        status_label.setText("trying to connect " + Config.getDefaultServer() + "..."));
    ser.setOnSucceeded((WorkerStateEvent event) ->
        status_label.setText("connected: " + networkServiceInt.getName()));
    ser.setOnFailed((WorkerStateEvent event) ->
        status_label.setText("unable to connect"));
    ser.start();
  }

  /**
   * Отправляет серверу команду {@link MessageType}
   * сервер возвращает List<FileInfo>
   * @throws IOException
   */
  public void forwardServerFolders(ActionEvent actionEvent) throws IOException {
    String path = server_textFlow.getText();
    if (path == null || path.isEmpty())
      //test
      path = "";
      //path = serverPathHistory.peek().toString();
    if (networkServiceInt == null) {
      connect(actionEvent);
    } else
    sendMessage(new Messages<>(MessageType.DIR, path));
  }

  private void sendMessage(Messages<?> messages) throws IOException {
    ((ObjectOutputStream) networkServiceInt.getOut()).writeObject(messages);
  }

  private void fillElements() {

    Platform.runLater(() -> TreeViewController.setupTreeView(tree_view));
  }


  private void serverRefresh(List<FileInfo> list) {
    server_panel.getItems().clear();
    server_panel.getItems().addAll(list);
    server_panel.sort();
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
    fillElements();
    clientPanelController = (ClientPanelController) client_panel_view.getProperties().get("ctrl");
    clientPanelController.setMainSceneController(this);
    clientPanelController.init();
    serverPathHistory = new Stack<>();

  }
}
