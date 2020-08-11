package org.owpk.IODataHandler;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.owpk.controller.UserDialog;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;
import org.owpk.network.IONetworkServiceImpl;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Отправляет форму с данными юзера на сервер, слушает ответ
 */
@Getter
@Setter
public class SignHandler extends AbsHandler{
  private final CountDownLatch doneLatch = new CountDownLatch(1);
  private String login;
  private String password;
  private String email;

  private void showDialog() {
    Platform.runLater(this::signDialog);
  }

  @Override
  protected void listen(Message<?> message) throws IOException {
    switch (message.getType()) {
      case OK:
        Platform.runLater(() -> UserDialog.confirmDialog(message.getPayload().toString(), null));
        IONetworkServiceImpl.getService().addHandlerToPipeline(new AuthHandler());
        handlerIsOver = true;
        break;
      case ERROR:
        Platform.runLater(() -> {
          UserDialog.errorDialog(message.getPayload().toString());
          showDialog();
        });
        break;
    }
    doneLatch.countDown();
  }

  @Override
  public void execute() throws InterruptedException {
    showDialog();
    doneLatch.await();
  }

  public void signDialog() {
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Sign");
    dialog.setHeaderText("Please enter login and password");

    ButtonType signButtonType = new ButtonType("Sign up", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(signButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField username = new TextField();
    username.setPromptText("Username");
    TextField email = new TextField();
    PasswordField password = new PasswordField();
    password.setPromptText("Password");
    username.setPromptText("Email");
    PasswordField confirmPassword = new PasswordField();
    password.setPromptText("Confirm password");
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
    dialog.setOnCloseRequest(x -> IONetworkServiceImpl.getService().disconnect());

    Platform.runLater(username::requestFocus);
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == signButtonType) {
        try {
          writeMessage(new UserInfo(MessageType.SIGN, username.getText(), password.getText(), email.getText()));
          new Thread(() -> {
            try {
              handlerIsOver = false;
              initDataListener();
            } catch (IOException | ClassNotFoundException e) {
              e.printStackTrace();
            }
          }).start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (dialogButton == ButtonType.CANCEL) {
        IONetworkServiceImpl.getService().disconnect();
      }
      return null;
    });
    dialog.showAndWait();
  }
}
