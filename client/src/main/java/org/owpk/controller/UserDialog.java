package org.owpk.controller;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.owpk.IODataHandler.SignHandler;
import org.owpk.app.ClientConfig;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;
import org.owpk.network.IONetworkServiceImpl;
import org.owpk.util.Config;

import java.io.IOException;
import java.util.Optional;


public class UserDialog {

  public static void warningDialog(String header, String content) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("Warning");
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  public static boolean confirmDialog(String header, String content) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Confirmation Dialog");
    alert.setHeaderText(header);
    alert.setContentText(content);
    Optional<ButtonType> result = alert.showAndWait();
    return result.get() == ButtonType.OK;
  }

  public static void errorDialog(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText("Error");
    alert.setContentText(msg);
    alert.showAndWait();
  }

  public static void infoDialog(String header, String msg) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText(header);
    alert.setContentText(msg);
    alert.showAndWait();
  }

  public static void showConnectionParameters() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Connection configuration");
    dialog.setHeaderText("Please, input connection parameters: ");

    ButtonType okBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    dialog.getDialogPane().getButtonTypes().setAll(okBtn, cancelBtn);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField host = new TextField();
    host.setPromptText("Host");
    TextField port = new TextField();
    port.setPromptText("Port");

    grid.add(new Label("Host name:"), 0, 0);
    grid.add(host, 1, 0);
    grid.add(new Label("Port address:"), 0, 1);
    grid.add(port, 1, 1);

    Node okButtonNode = dialog.getDialogPane().lookupButton(okBtn);
    okButtonNode.setDisable(true);

    host.textProperty().addListener((observable, oldValue, newValue) -> okButtonNode.setDisable(newValue.trim().isEmpty()));

    dialog.getDialogPane().setContent(grid);

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.get() == okBtn) {
      ClientConfig.getConfig().writeProperty(Config.ConfigParameters.HOST, host.getText());
      ClientConfig.getConfig().writeProperty(Config.ConfigParameters.PORT, port.getText());
    }
  }
}