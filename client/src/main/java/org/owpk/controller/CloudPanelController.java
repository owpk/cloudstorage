package org.owpk.controller;

import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.owpk.IODataHandler.AuthException;
import org.owpk.app.ClientConfig;
import org.owpk.message.DataInfo;
import org.owpk.message.MessageType;
import org.owpk.message.Message;
import org.owpk.IODataHandler.InputDataHandler;
import org.owpk.network.NetworkServiceFactory;
import org.owpk.network.NetworkServiceInt;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

public class CloudPanelController {
  @FXML private ProgressBar progress_cloud;
  @FXML private TableView<FileInfo> server_panel;
  @FXML private Button back_btn;
  @FXML private Button forward_btn;
  @FXML private Button update_btn;
  @FXML private Button connect_btn;
  @FXML private TextField cloud_text_field;
  private Stack<Path> cloudBackInHistoryStack;
  private Stack<Path> cloudForwardInHistoryStack;

  private NetworkServiceInt networkServiceInt;
  private MainSceneController mainSceneController;

  private Callback<List<FileInfo>> cloudTableCallback;
  private Callback<String> statusLabelCallback;
  private Callback<Double> progressBarCallback;


  /**
   * метод вызывается при нажатии на кнопку "connect"
   * {@link NetworkServiceFactory} возвращает {@link NetworkServiceInt},
   * который создает подключение клиента к серверу,
   * данные для созданного подключения обслуживает {@link InputDataHandler}
   */
  public void connect() {
    Service<Void> ser = new Service<Void>() {
      @Override
      protected Task<Void> createTask() {
        return new Task<Void>() {
          @Override
          protected Void call() throws InterruptedException {
            try {
              networkServiceInt = NetworkServiceFactory.getHandler(ClientConfig.getDefaultServer());
              networkServiceInt.initHandlers(cloudTableCallback,
                  statusLabelCallback,
                  progressBarCallback,
                  mainSceneController.getClientPanelController().getRefreshPanelCallback());
              networkServiceInt.connect();
              updateServerFolders();
            } catch (AuthException | IOException | ClassNotFoundException e) {
              disconnect();
              networkServiceInt = null;
              throw new InterruptedException(e.getMessage());
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
    ser.setOnFailed((WorkerStateEvent event) -> {
        mainSceneController.setStatusLabel("unable to connect");
        UserDialog.errorDialog(event.getSource().getException().getMessage());
        });
    ser.start();
  }

  private void upload(File f) throws IOException {
    Service<Void> ser = new Service<Void>() {
      @Override
      protected Task<Void> createTask() {
        return new Task<Void>() {
          @Override
          protected Void call() throws InterruptedException, IOException {
            DataInfo[] bufferedData = FileUtility.getChunkedFile(f, MessageType.UPLOAD);
            float counter;
            for (int i = 0; i < bufferedData.length; i++) {
              sendMessage(bufferedData[i]);
              counter = (float) i / bufferedData.length;
              float finalCounter = counter;
              Platform.runLater(() -> progress_cloud.setProgress(finalCounter));
            }
            return null;
          }
        };
      }
    };
    //выводим информацию в текс лейбл по результату выполнения
    ser.setOnRunning((WorkerStateEvent event) ->
        mainSceneController.setStatusLabel("uploading..."));
    ser.setOnSucceeded((WorkerStateEvent event) -> {
      mainSceneController.setStatusLabel("done");
      sendMessage(new Message<String>(MessageType.DIR));
      progress_cloud.setProgress(0);
    });
    ser.setOnFailed((WorkerStateEvent event) ->
        mainSceneController.setStatusLabel("failed"));
    ser.start();
  }

  /**
   * Отправляет серверу команду {@link MessageType}
   * сервер возвращает List<FileInfo>
   */
  public void updateServerFolders() {
    if (networkServiceInt == null) {
      connect();
    } else
      sendMessage(new Message<String>(MessageType.DIR));
  }

  private void sendMessage(Message<?> messages) {
    try {
      ((ObjectEncoderOutputStream) networkServiceInt.getOut()).writeObject(messages);
    } catch (IOException e) {
      e.printStackTrace();
    }
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
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
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

  /**
   * Добавляет колонку с кнопкой "скачать" в cloud таблицу
   * Посылает на сервер команду {@link MessageType} DOWNLOAD и имя файла
   */
  private void initDownloadAction() {
    TableColumn<FileInfo, FileInfo.FileType> server_column_action = new TableColumn<>("Action");
    server_column_action.setCellValueFactory(new PropertyValueFactory<>("fileType"));


    javafx.util.Callback<TableColumn<FileInfo, FileInfo.FileType>, TableCell<FileInfo, FileInfo.FileType>> cellFactory
        = param -> new TableCell<FileInfo, FileInfo.FileType>() {
          final Button btn = new Button();
          {
            btn.setMaxWidth(30);
            btn.setPadding(Insets.EMPTY);
            btn.setGraphic(new IconBuilder()
                .setIconImage("download")
                .setFitHeight(15)
                .build());
          }
          @Override
          protected void updateItem(FileInfo.FileType item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == FileInfo.FileType.DIRECTORY) {
              setGraphic(null);
            } else {
              btn.setOnAction(event -> {
                FileInfo info = getTableView().getItems().get(getIndex());
                sendMessage(new Message<>(MessageType.DOWNLOAD, info.getFilename()));
              });
              setGraphic(btn);
            }
            setText(null);
          }
        };
    server_column_action.setCellFactory(cellFactory);
    server_panel.getColumns().add(server_column_action);
  }

  public void onUpBtnClicked(ActionEvent actionEvent) {
  }

  public void init() {
    initListeners();
    connect();
    cloudTableCallback = this::serverRefresh;
    initDownloadAction();
    statusLabelCallback = s -> mainSceneController.setStatusLabel(s);
    progressBarCallback = i -> Platform.runLater(() -> progress_cloud.setProgress(i));
  }

}
