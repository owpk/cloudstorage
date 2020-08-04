package org.owpk.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import org.owpk.app.Callback;
import org.owpk.app.Config;
import org.owpk.util.FileInfo;
import org.owpk.util.FileUtility;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;
import java.util.stream.Collectors;

public class ClientPanelController {
  @FXML private TableView<FileInfo> client_panel;
  @FXML private Button client_forward_btn;
  @FXML private TextField client_textFlow;
  @FXML private ComboBox<String> disk_list;
  @FXML private Button client_back;
  @FXML private VBox client_panel_vbox;
  private MainSceneController mainSceneController;
  private Callback<String> textFlowCallback;
  public Stack<Path> clientBackInHistoryStack;
  public Stack<Path> clientForwardInHistoryStack;
  private FileChooser fileChooser;
  private Desktop desktop;

  /**
   * кнопка вперед по истории
   */
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

  /**
   * кнопка назад по истории
   */
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

  /**
   * кнопка вверх по директории
   */
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

  private void resetStatusLabel() {
    mainSceneController.setStatusLabel("");
  }

  /**
   * обновляет таблицу локальных файлов
   */
  private void clientRefresh(Path p) {
    try {
      resetStatusLabel();
      client_panel.getItems().clear();
      client_panel.getItems().addAll(Files
          .list(p)
          .parallel()
          .map(FileInfo::new)
          .collect(Collectors.toList()));
      client_panel.sort();
    } catch (IOException e) {
      mainSceneController.setStatusLabel("can't open");
      e.printStackTrace();
    }
  }

  public void clientRefresh() {
    clientRefresh(clientBackInHistoryStack.peek());
  }

  private void showBhistory() {
    System.out.println(clientBackInHistoryStack + " <--- back");
  }
  private void showFhistory() {
    System.out.println(clientForwardInHistoryStack + " <--- forward");
  }

  private void initCallbacks() {
    textFlowCallback = s -> { Platform.runLater(()-> {
      client_textFlow.setText(s);
      clientBackInHistoryStack.push(Paths.get(s));
    }); clientRefresh(Paths.get(s)); };
    TreeViewController.setTextFlowCallBack(textFlowCallback);
  }

  private void initListeners() {
    initDragAndDropListeners();

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
    client_panel.setOnMouseClicked(x -> {
      if (x.getClickCount() == 2 && x.getButton() == MouseButton.PRIMARY) {
        FileInfo f = client_panel.getSelectionModel().getSelectedItem();
        if (f.getFileType() == FileInfo.FileType.DIRECTORY) {
          Path p = f.getPath();
          clientRefresh(p);
          client_textFlow.setText(p.toString());
          clientBackInHistoryStack.push(p);
          showBhistory();
        } else {
          File file = f.getPath().toFile();
          if (file.exists()) {
            openFile(file);
          }
        }
      }
    });
  }

  /**
   * инициализирует DragAndDrop слушателей
   */
  private FileInfo tempItem;
  private Path targetDirectory;
  private void initDragAndDropListeners() {

    client_panel.setOnDragDetected(x -> {
      FileInfo f = client_panel.getSelectionModel().getSelectedItem();
      File from;
      if (f != null) {
        from = f.getPath().toFile();
        System.out.println(f.getPath());
        Dragboard db = client_panel.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(from.getAbsolutePath());
        db.setContent(content);
        x.consume();
      }
    });

    client_panel.setOnDragOver(x -> {
      x.acceptTransferModes(TransferMode.ANY);
    });

    //здесь отслеживается target путь, это можно сделать только через RowFactory
    client_panel.setRowFactory(x -> {
      TableRow<FileInfo> row = new TableRow<>();
      row.setOnDragDropped(event -> {
        tempItem = row.getItem();
      });
      return row;
    });

    //если событие пришло из другой панели то нужно взять текущий путь из истории в этой панеле
    //иначе мы не сможем узнать target путь
    client_panel.setOnDragEntered(x -> {
        targetDirectory = clientBackInHistoryStack.peek();
    });

    //если событие завершено берем source путь из TreeItem, используем Files.move(),
    // в этом случае обертку FileUtils, в которой дополнительно отслеживается -
    // перемещается папка или файл и возможные Exception,
    // после того как все успешно переместилось обновляем оба TableView через MainController
    // иначе обновляется только одна таблица на которой фокус
    client_panel.setOnDragDropped(x -> {
      x.acceptTransferModes(TransferMode.ANY);
      Dragboard db = x.getDragboard();
      boolean success = false;
      if (db.hasString()) {
        Path source = Paths.get(db.getString());
        Path target;
        if (tempItem != null) {
          target = tempItem.getPath();
        } else target = targetDirectory;
        if (!source.equals(target)) {
          System.out.println("-Move from: " + source);
          System.out.println("-Move to: " + target);
          try {
            FileUtility.move(source, target);
            mainSceneController.refreshAllClientPanels();
            mainSceneController.setStatusLabel("done");
            success = true;
          } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Can't move file");
            alert.setContentText(e.toString());
            alert.showAndWait();
            e.printStackTrace();
          }
        }
      }
      tempItem = null;
      x.setDropCompleted(success);
      x.consume();
    });
  }

  private void openFile(File file) {
    try {
        desktop.open(file);
    } catch (IOException ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }

  private void fillElements() {
    FileSystems.getDefault().getFileStores().forEach(x -> disk_list.getItems().add(x.toString()));
    disk_list.getSelectionModel().select(0);
  }

  public void setMainSceneController(MainSceneController mainSceneController) {
    this.mainSceneController = mainSceneController;
  }

  @SneakyThrows
  public void init() {
    desktop = Desktop.getDesktop();
    fileChooser = new FileChooser();
    clientBackInHistoryStack = new Stack<>();
    clientForwardInHistoryStack = new Stack<>();
    fillElements();
    final Path ROOT_PATH = Config.getSourceRoot();
    clientBackInHistoryStack.push(ROOT_PATH);
    clientRefresh(ROOT_PATH);
    client_textFlow.setText(ROOT_PATH.toString());
    initCallbacks();
    initListeners();
    client_panel.setPlaceholder(new Label(""));
  }
}
