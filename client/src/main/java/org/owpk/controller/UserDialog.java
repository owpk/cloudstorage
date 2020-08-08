package org.owpk.controller;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
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

  public static Optional<Pair<String, String>> loginDialog() {
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Login Dialog");
    dialog.setHeaderText("Look, a Custom Login Dialog");

    ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField username = new TextField();
    username.setPromptText("Username");
    PasswordField password = new PasswordField();
    password.setPromptText("Password");

    grid.add(new Label("Username:"), 0, 0);
    grid.add(username, 1, 0);
    grid.add(new Label("Password:"), 0, 1);
    grid.add(password, 1, 1);

    Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
    loginButton.setDisable(true);

    username.textProperty().addListener((observable, oldValue, newValue) -> {
      loginButton.setDisable(newValue.trim().isEmpty());
    });

    dialog.getDialogPane().setContent(grid);

    Platform.runLater(username::requestFocus);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        return new Pair<>(username.getText(), password.getText());
      }
      return null;
    });

    return dialog.showAndWait();
  }
}