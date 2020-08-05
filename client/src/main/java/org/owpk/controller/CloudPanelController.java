package org.owpk.controller;

import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.owpk.app.Callback;
import org.owpk.app.ClientConfig;
import org.owpk.message.DataInfo;
import org.owpk.message.MessageType;
import org.owpk.message.Message;
import org.owpk.network.InputDataHandler;
import org.owpk.network.NetworkServiceFactory;
import org.owpk.network.NetworkServiceInt;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

public class CloudPanelController {
  private static final int BUFFER_SIZE = 8192;
  @FXML private TableView<FileInfo> server_panel;
  @FXML private Button back_btn;
  @FXML private Button forward_btn;
  @FXML private Button up_btn;
  @FXML private TextField cloud_text_field;
  @FXML private Button connect_btn;
  public Stack<Path> cloudBackInHistoryStack;
  public Stack<Path> cloudForwardInHistoryStack;

  private NetworkServiceInt networkServiceInt;
  private MainSceneController mainSceneController;

  private Callback<List<FileInfo>> cloudTableCallback;
  private Callback<String> statusLabelCallback;

  /**
   * метод вызывается при нажатии на кнопку "connect"
   * {@link NetworkServiceFactory} возвращает {@link NetworkServiceInt},
   * который создает подключение клиента к серверу,
   * данные для созданного подключения обслуживает {@link InputDataHandler}
   */
  public void connect(ActionEvent actionEvent) {
    Service<Void> ser = new Service<Void>() {
      @Override
      protected Task<Void> createTask() {
        return new Task<Void>() {
          @Override
          protected Void call() throws InterruptedException, IOException {
            try {
              networkServiceInt = NetworkServiceFactory.getHandler(ClientConfig.getDefaultServer());
              networkServiceInt.connect();
              InputDataHandler inputDataHandler =
                  new InputDataHandler(networkServiceInt,
                      cloudTableCallback, statusLabelCallback);
              networkServiceInt.initDataHandler(inputDataHandler);
              updateServerFolders(actionEvent);
            } catch (IOException e) {
              disconnect();
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
        mainSceneController.setStatusLabel("trying to connect " + ClientConfig.getDefaultServer() + "..."));
    ser.setOnSucceeded((WorkerStateEvent event) ->
        mainSceneController.setStatusLabel("connected: " + networkServiceInt.getName()));
    ser.setOnFailed((WorkerStateEvent event) ->
        mainSceneController.setStatusLabel("unable to connect"));
    ser.start();
  }

  /**
   * Отправляет серверу команду {@link MessageType}
   * сервер возвращает List<FileInfo>
   * @throws IOException
   */
  public void updateServerFolders(ActionEvent actionEvent) throws IOException {
    String path = cloud_text_field.getText();
    if (path == null || path.isEmpty())
      //test
      path = "";
    //path = serverPathHistory.peek().toString();
    if (networkServiceInt == null) {
      connect(actionEvent);
    } else
      sendMessage(new Message<String>(MessageType.DIR));
  }

  private void sendMessage(Message<?> messages) throws IOException {
    ((ObjectEncoderOutputStream) networkServiceInt.getOut()).writeObject(messages);
  }

  private void serverRefresh(List<FileInfo> list) {
    server_panel.getItems().clear();
    server_panel.getItems().addAll(list);
    server_panel.sort();
  }

  private void initListeners() {
    final FileInfo[] tempItem = new FileInfo[1];

    server_panel.setRowFactory(x -> {
      TableRow<FileInfo> row = new TableRow<>();
      row.setOnDragDropped(event -> {
        tempItem[0] = row.getItem();
      });
      return row;
    });

    server_panel.setOnDragOver(x -> {
      x.acceptTransferModes(TransferMode.ANY);
    });

    server_panel.setOnDragDropped(x -> {
      Dragboard db = x.getDragboard();
      File f = new File(db.getString());
      if (f.isFile()) {
        try {
          upload(f);
          sendMessage(new Message<String>(MessageType.DIR));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void upload(File f) throws IOException {
    DataInfo[] bufferedData = FileUtility.getChunkedFile(f, MessageType.UPLOAD);
    for (DataInfo data: bufferedData)
      sendMessage(data);
  }

  public void setMainSceneController(MainSceneController mainSceneController) {
    this.mainSceneController = mainSceneController;
  }

  void disconnect() {
    if (networkServiceInt != null) {
      try {
        networkServiceInt.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void init() {
    initListeners();
    cloudTableCallback = x -> {
      server_panel.getItems().addAll(x);
      server_panel.sort();
    };
    statusLabelCallback = s -> mainSceneController.setStatusLabel(s);
  }
}
