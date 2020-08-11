package org.owpk.IODataHandler;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.owpk.controller.UserDialog;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class SignHandler extends AbsHandler{
  private final CountDownLatch doneLatch = new CountDownLatch(1);
  private String login;
  private String password;
  private String email;

  public void showDialog() {
    Platform.runLater(()->{
      signDialog();
      doneLatch.countDown();
    });
  }

  public void tryToSign() throws IOException, ClassNotFoundException, InterruptedException {
    doneLatch.await();
    writeMessage(new UserInfo(MessageType.SIGN, login, password, email));
    initDataListener();
  }

  @Override
  protected void listen(Message<?> message) throws IOException {
    //TODO
  }

  public static Optional<Pair<String, String>> signDialog() {
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Sign");
    dialog.setHeaderText("Please enter login and password");

    ButtonType loginButtonType = new ButtonType("Sign", ButtonBar.ButtonData.OK_DONE);
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

    username.textProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue.trim().isEmpty()));

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
