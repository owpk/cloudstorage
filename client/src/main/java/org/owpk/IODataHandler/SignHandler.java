package org.owpk.IODataHandler;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import lombok.Setter;
import org.owpk.controller.UserDialog;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;
import org.owpk.network.IONetworkServiceImpl;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Отправляет форму с данными юзера на сервер для регистрации, слушает ответ
 * @see #initDataListener()
 * @see #listen(Message)
 */
@Getter
@Setter
public class SignHandler extends AbsHandler{
  private final CountDownLatch doneLatch = new CountDownLatch(1);

  private void showDialog() {
    Platform.runLater(this::signDialog);
  }

  @Override
  protected void listen(Message<?> message) throws IOException {
    switch (message.getType()) {
      case OK:
        Platform.runLater(() -> {
          UserDialog.confirmDialog(message.getPayload().toString(), null);
          IONetworkServiceImpl.getService().addHandlerToPipeline(new AuthHandler());
          setHandlerOver(true);
          doneLatch.countDown();
        });
        break;
      case ERROR:
        Platform.runLater(() -> {
          UserDialog.errorDialog(message.getPayload().toString());
          showDialog();
          doneLatch.countDown();
        });
        break;
    }
  }

  //sync
  @Override
  public void execute() throws InterruptedException {
    showDialog();
    doneLatch.await();
  }

  public void signDialog() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Sign");
    dialog.setHeaderText("Please enter login and password");

    ButtonType signButtonType = new ButtonType("Sign up", ButtonBar.ButtonData.OK_DONE);
    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    dialog.getDialogPane().getButtonTypes().setAll(signButtonType, buttonTypeCancel);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField username = new TextField();
    username.setPromptText("Username");
    TextField email = new TextField();
    email.setPromptText("Email");
    PasswordField password = new PasswordField();
    password.setPromptText("Password");
    PasswordField confirmPassword = new PasswordField();
    confirmPassword.setPromptText("Confirm password");
    grid.add(new Label("Username:"), 0, 0);
    grid.add(username, 1, 0);
    grid.add(new Label("Email:"), 0, 1);
    grid.add(email, 1, 1);
    grid.add(new Label("Password:"), 0, 3);
    grid.add(password, 1, 3);
    grid.add(new Label("Confirm password:"), 0, 4);
    grid.add(confirmPassword, 1, 4);

    Node signButton = dialog.getDialogPane().lookupButton(signButtonType);
    signButton.setDisable(true);
    username.textProperty().addListener((observable, oldValue, newValue) -> signButton.setDisable(newValue.trim().isEmpty()));
    dialog.getDialogPane().setContent(grid);

    Platform.runLater(username::requestFocus);
    Optional<ButtonType> result = dialog.showAndWait();
    if (result.get() == signButtonType) {
        try {
          writeMessage(new UserInfo(MessageType.SIGN, username.getText(), password.getText(), email.getText()));
          new Thread(() -> {
            try {
              setHandlerOver(false);
              initDataListener();
            } catch (IOException | ClassNotFoundException e) {
              e.printStackTrace();
            }
          }).start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      else {
        IONetworkServiceImpl.getService().disconnect();
        doneLatch.countDown();
      }
  }
}
