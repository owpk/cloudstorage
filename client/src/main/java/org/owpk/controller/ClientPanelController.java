package org.owpk.controller;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.owpk.app.Callback;
import org.owpk.app.Config;
import org.owpk.util.FileInfo;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.stream.Collectors;

public class ClientPanelController {
  @FXML public TableView<FileInfo> client_panel;
  @FXML public Button client_forward_btn;
  @FXML public TextField client_textFlow;
  @FXML public ComboBox<String> disk_list;
  @FXML public Button client_back;
  public VBox client_panel_vbox;
  private MainSceneController mainSceneController;
  private Callback<String> textFlowCallback;
  public Stack<Path> clientBackInHistoryStack;
  public Stack<Path> clientForwardInHistoryStack;
  private static Path ROOT_PATH;
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
    Platform.runLater(() -> mainSceneController.status_label.setText(""));
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
      Platform.runLater(() -> mainSceneController.status_label.setText("can't open"));
      e.printStackTrace();
    }
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

    client_panel.setOnDragDetected(x -> {
      System.out.println("dragging");
      FileInfo f = client_panel.getSelectionModel().getSelectedItem();
      File from;
      if (f != null)
       from = f.getPath().toFile();
    });

    client_panel.setOnDragEntered(x -> {

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

  private void openFile(File file) {
    try {
        desktop.open(file);
    } catch (IOException ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }

  private void fillElements() {
    FileSystems.getDefault().getFileStores().forEach(x -> disk_list.getItems().add(x.toString()));
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
    ROOT_PATH = Config.getSourceRoot();
    clientBackInHistoryStack.push(ROOT_PATH);
    clientRefresh(ROOT_PATH);
    client_textFlow.setText(ROOT_PATH.toString());
    initCallbacks();
    initListeners();
    client_panel.setPlaceholder(new Label(""));
  }


}
