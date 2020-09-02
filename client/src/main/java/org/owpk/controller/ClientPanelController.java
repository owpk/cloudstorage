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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.util.Callback;
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
  private final Logger log = LogManager.getLogger(ClientPanelController.class.getName());
  @FXML private TableView<FileInfo> client_panel;
  @FXML private Button client_forward_btn;
  @FXML private Button client_back;
  @FXML private TextField client_textFlow;
  @FXML private ComboBox<String> disk_list;
  @FXML private VBox client_panel_vbox;

  private MainSceneController mainSceneController;

  private Stack<Path> clientBackInHistoryStack;
  private Stack<Path> clientForwardInHistoryStack;

  private Callback<String> textFlowCallback;
  private Callback<Path> refreshPanel;

  private FileChooser fileChooser;
  private Desktop desktop;

  /**
   * go forward button
   */
  @FXML
  public void onForwardInClientHistory() {
    if (clientForwardInHistoryStack.size() > 0) {
      Path p = clientForwardInHistoryStack.peek();
      clientBackInHistoryStack.push(clientForwardInHistoryStack.pop());
      clientRefresh(p);
    }
  }

  /**
   * go back button
   */
  @FXML
  public void onBackInClientHistory(ActionEvent actionEvent) {
    if (clientBackInHistoryStack.size() > 1) {
      clientForwardInHistoryStack.push(clientBackInHistoryStack.pop());
      Path p = clientBackInHistoryStack.peek();
      clientRefresh(p);
    }
  }

  /**
   * directory up button
   */
  @FXML
  public void onUpBtnClicked(ActionEvent actionEvent) {
    final Path p = clientBackInHistoryStack.peek().getParent();
    if (p != null) {
      clientRefresh(p);
      clientBackInHistoryStack.push(p);
    }
  }

  public Stack<Path> getHistory() {
    return clientBackInHistoryStack;
  }

  private void resetStatusLabel() {
    mainSceneController.setStatusLabel("");
  }

  /**
   * Update local table
   * @see FileInfo
   */
  public void clientRefresh(Path p) {
    try {
      resetStatusLabel();
      client_textFlow.setText(p.toString());
      client_panel.getItems().clear();
      client_panel.getItems().addAll(Files
          .list(p)
          .parallel()
          .map(FileInfo::new)
          .collect(Collectors.toList()));
      client_panel.sort();
    } catch (IOException e) {
      log.error(e);
      mainSceneController.setStatusLabel("can't open");
      e.printStackTrace();
    }
  }

  public void clientRefresh() {
    clientRefresh(clientBackInHistoryStack.peek());
  }

  private void initCallbacks() {
    textFlowCallback = s -> { Platform.runLater(()-> {
      client_textFlow.setText(s);
      clientBackInHistoryStack.push(Paths.get(s));
    }); clientRefresh(Paths.get(s)); };
    TreeViewController.setTextFlowCallBack(textFlowCallback);
    refreshPanel = this::clientRefresh;
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
          final Path p = f.getPath();
          clientRefresh(p);
          client_textFlow.setText(p.toString());
          clientBackInHistoryStack.push(p);
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
   * Initializing drag-and-drop listeners.
   * Detect drag over, drag entered and drag dropped events.
   */
  private Path targetDirectory;
  private void initDragAndDropListeners() {
    final FileInfo[] tempItem = new FileInfo[1];

    client_panel.setOnDragDetected(x -> {
      final FileInfo f = client_panel.getSelectionModel().getSelectedItem();
      File from;
      if (f != null) {
        from = f.getPath().toFile();
        final Dragboard db = client_panel.startDragAndDrop(TransferMode.MOVE);
        final ClipboardContent content = new ClipboardContent();
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
      final TableRow<FileInfo> row = new TableRow<>();
      row.setOnDragDropped(event -> tempItem[0] = row.getItem());
      return row;
    });

    //если событие пришло из другой панели то нужно взять текущий путь из истории в этой панеле
    //иначе мы не сможем узнать target путь
    client_panel.setOnDragEntered(x -> {
        targetDirectory = clientBackInHistoryStack.peek();
    });

    //если событие завершено, берем source путь из TreeItem, используем Files.move(),
    // в этом случае обертку FileUtils, в которой дополнительно отслеживается -
    // перемещается папка или файл и возможные исключения,
    // после того как все успешно переместилось обновляем оба TableView через MainController
    // иначе обновляется только одна таблица на которой фокус
    client_panel.setOnDragDropped(x -> {
      x.acceptTransferModes(TransferMode.ANY);
      final Dragboard db = x.getDragboard();
      boolean success = false;
      if (db.hasString()) {
        final Path source = Paths.get(db.getString());
        final Path target = tempItem[0] != null ? tempItem[0].getPath() : targetDirectory;
        if (!source.equals(target)) {
          try {
            FileUtility.move(source, target);
            mainSceneController.refreshAllClientPanels();
            mainSceneController.setStatusLabel("done");
            success = true;
          } catch (IOException e) {
            UserDialog.errorDialog("can't move file \n" + e.getLocalizedMessage());
            e.printStackTrace();
          }
        }
      }
      tempItem[0] = null;
      x.setDropCompleted(success);
      x.consume();
    });
  }

  private void openFile(File file) {
    try {
        desktop.open(file);
    } catch (IOException ex) {
      log.error(ex);
    }
  }

  private void fillElements() {
    FileSystems.getDefault().getFileStores().forEach(x -> disk_list.getItems().add(x.toString()));
    disk_list.getSelectionModel().select(0);
  }

  public void setMainSceneController(MainSceneController mainSceneController) {
    this.mainSceneController = mainSceneController;
  }

  public Callback<Path> getRefreshPanelCallback() {
    return refreshPanel;
  }

  @SneakyThrows
  public void init() {
    desktop = Desktop.getDesktop();
    fileChooser = new FileChooser();
    clientBackInHistoryStack = new Stack<>();
    clientForwardInHistoryStack = new Stack<>();
    fillElements();
    final Path START_PATH = mainSceneController.getConfig().getStartPath();
    clientBackInHistoryStack.push(START_PATH);
    clientRefresh(START_PATH);
    client_textFlow.setText(START_PATH.toString());
    initCallbacks();
    initListeners();
    client_panel.setPlaceholder(new Label(""));
  }

}
