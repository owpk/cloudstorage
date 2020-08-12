package org.owpk.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import org.owpk.app.ClientConfig;
import org.owpk.util.Config;

import java.util.Optional;


public class UserDialog {

  public static void showDwnldDirSetupDialog(String message, ClientConfig config) {
    TextInputDialog dialog = new TextInputDialog("walter");
    dialog.setTitle("Configure");
    dialog.setHeaderText(message);
    dialog.setContentText("Please enter default download directory:");

    Optional<String> result = dialog.showAndWait();

    result.ifPresent(name -> config.writeProperty(
        Config.ConfigParameters.DOWNLOAD_DIR, result.get()));
  }

  public static void warningDialog(String header, String content) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("Warning");
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  public static void confirmDialog(String header, String content) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Confirmation Dialog");
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  public static void errorDialog(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText("Error");
    alert.setContentText(msg);
    alert.showAndWait();
  }

}