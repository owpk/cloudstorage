package org.owpk.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

  public static boolean confirmDialog(String folder) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Confirmation Dialog");
    alert.setHeaderText("Did not found specified download directory");
    alert.setContentText("Create default download folder? : \n" + folder);

    Optional<ButtonType> result = alert.showAndWait();
    return result.get() == ButtonType.OK;
  }

  public static void errorDialog(String msg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText("Look, an Error Dialog");
    alert.setContentText(msg);
    alert.showAndWait();
  }
}