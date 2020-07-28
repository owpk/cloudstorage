package org.owpk.controller;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * основной контроллер
 */
public class Controller implements Initializable {

  @FXML public MenuBar drag_menu;
  @FXML public Button shut_down_btn;
  @FXML public Button roll_down_btn;
  @FXML public Button roll_up_btn;
  @FXML public TableView<FileInfo> server_panel;
  @FXML public TableView<FileInfo> client_panel;
  @FXML public Button server_forward_btn;
  @FXML public Button client_forward_btn;
  @FXML public TextField server_textFlow;
  @FXML public TextField client_textFlow;
  @FXML public Label status_label;
  @FXML public Button connect_btn;
  @FXML public ComboBox<String> disk_list;
  @FXML public Button client_back;
  @FXML public VBox main_window;
  @FXML public VBox tree_window;
  @FXML public TreeView<String> tree_view;
  private Callback<List<FileInfo>> serverTableCallback;
  private Callback<String> serverStatusLabel;
  private NetworkServiceInt networkServiceInt;
  private Stack<Path> clientBackInHistoryStack;
  private Stack<Path> clientForwardInHistoryStack;
  private Stack<Path> serverPathHistory;
  private Stage stage;
  private static Path ROOT_PATH;
  private double xOffset = 0;
  private double yOffset = 0;

  public void setStageAndSetupListeners(Stage stage) {
    this.stage = stage;
    ResizeHelper.addResizeListener(stage);

    //кнопка закрыть
    shut_down_btn.setOnMouseClicked(event -> {
      Config.setSourceRoot(clientBackInHistoryStack.peek().toString());
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
   * или при первом нажатии на кнопку "refresh".
   * {@link NetworkHandlerFactory} возвращает {@link NetworkServiceInt},
   * который создает подключение клиента к серверу,
   * потоки данных для созданного подключения обслуживает {@link InputDataHandler}
   * команда выполняется в отедльном сервисном потоке, чтобы не мешать остальному интерфейсу
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

  private void clientRefresh(Path p) {
    try {
      client_panel.getItems().clear();
      client_panel.getItems().addAll(Files
          .list(p)
          .map(FileInfo::new)
          .collect(Collectors.toList()));
      client_panel.sort();
    } catch (IOException e) {
      status_label.setText(e.getClass().getSimpleName());
      e.printStackTrace();
    }
  }

  private void resetStatusLabel(int i) {
    if (i == 0) client_textFlow.setText("");
    else server_textFlow.setText("");
  }

  @FXML
  public void onForwardInClientHistory() {
      if (clientForwardInHistoryStack.size() > 0) {
        Path p = clientForwardInHistoryStack.peek();
        clientBackInHistoryStack.push(clientForwardInHistoryStack.pop());
        clientRefresh(p);
        client_textFlow.setText(p.toString());
      }
    showBhistory();
  }

  @FXML
  public void onBackInClientHistory(ActionEvent actionEvent) {
      if (clientBackInHistoryStack.size() > 1) {
        clientForwardInHistoryStack.push(clientBackInHistoryStack.pop());
        Path p = clientBackInHistoryStack.peek();
        clientRefresh(p);
        client_textFlow.setText(p.toString());
      }
    showFhistory();
  }

  @FXML
  public void onUpBtnClicked(ActionEvent actionEvent) {
    Path p = clientBackInHistoryStack.peek().getParent();
    if (p != null) {
      clientRefresh(p);
      clientBackInHistoryStack.push(p);
      client_textFlow.setText(p.toString());
      showBhistory();
    }
  }

  private void showBhistory() {
    System.out.println(clientBackInHistoryStack + " <--- back");
  }

  private void showFhistory() {
    System.out.println(clientForwardInHistoryStack + " <--- forward");
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

  private void fillCombobox() {
    FileSystems.getDefault().getFileStores().forEach(x -> disk_list.getItems().add(x.toString()));
  }

  private void fillTreeView() {
//    Arrays.stream(File.listRoots())
//        .forEach(x ->
    Platform.runLater(() -> {
        tree_view.setRoot(getNodesForDirectory(
            new File("C:\\Users")));
    });
  }

  public TreeItem<String> getNodesForDirectory(File directory) {
    TreeItem<String> root = new TreeItem<>(directory.getName());
    File[] ff = directory.listFiles();
    new Thread(() -> {
      for (File f : ff) {
        if (f.isDirectory() && f.listFiles() != null && !f.isHidden() && f.canRead()) {
          root.getChildren().add(getNodesForDirectory(f));
        } else {
          root.getChildren().add(new TreeItem<>(f.getName()));
        }
      }
    }).start();
    return root;
  }

  private void initCallbacks() {
    serverTableCallback = s -> Platform.runLater(() -> serverRefresh(s));
    serverStatusLabel = s -> Platform.runLater(() -> status_label.setText(s));
  }

  @SneakyThrows
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fillTreeView();
    fillCombobox();
    client_textFlow.setOnKeyPressed(event -> {
      Path p;
      if (event.getCode() == KeyCode.ENTER) {
          p = Paths.get(client_textFlow.getText());
        if (Files.exists(p)) {
          clientRefresh(p);
          clientBackInHistoryStack.push(p);
        }
      }
    });
    clientBackInHistoryStack = new Stack<>();
    clientForwardInHistoryStack = new Stack<>();
    serverPathHistory = new Stack<>();
    ROOT_PATH = Config.getSourceRoot();
    clientBackInHistoryStack.push(ROOT_PATH);
    clientRefresh(ROOT_PATH);
    client_textFlow.setText(ROOT_PATH.toString());
    initCallbacks();
    client_panel.setPlaceholder(new Label(""));
    server_panel.setPlaceholder(new Label(""));
    client_panel.setOnMouseClicked(x -> {
      if (x.getClickCount() == 2 && x.getButton() == MouseButton.PRIMARY) {
        FileInfo f = client_panel.getSelectionModel().getSelectedItem();
        if (f.getFileType() == FileInfo.FileType.DIRECTORY) {
          Path p = Paths.get(client_textFlow.getText() + "\\" + f.getFilename());
          clientRefresh(p);
          client_textFlow.setText(p.toString());
          clientBackInHistoryStack.push(p);
          showBhistory();
        }
      }
    });
  }

}
