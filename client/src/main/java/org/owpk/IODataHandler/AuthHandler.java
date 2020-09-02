package org.owpk.IODataHandler;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.controller.UserDialog;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;
import org.owpk.network.IONetworkServiceImpl;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * Отпарвляет форму с данными на сервер для аутентификации, слушает ответ
 * @see #initDataListener()
 * @see #listen(Message)
 */
@Getter
@Setter
public class AuthHandler extends AbsHandler {
  private final Logger log = LogManager.getLogger(AuthHandler.class.getName());
  private final CountDownLatch doneLatch = new CountDownLatch(1);

  private String hash(String input) {
    return DigestUtils.sha256Hex(input);
  }

  private void showDialog() {
    Platform.runLater(() -> {
      loginDialog();
      doneLatch.countDown();
    });
  }

  @Override
  protected void listen(Message<?> msg) {
    if (msg.getType() == MessageType.OK) {
      setHandlerOver(true);
      IONetworkServiceImpl.getService().addMainDataHandler();
    } else if (msg.getType() == MessageType.ERROR) {
      UserDialog.errorDialog((String) msg.getPayload());
      IONetworkServiceImpl.getService().addHandlerToPipeline(new AuthHandler());
      setHandlerOver(true);
    }
  }

  //sync
  @Override
  public void execute() throws InterruptedException {
    showDialog();
    doneLatch.await();
  }

  private void loginDialog() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Login");
    dialog.setHeaderText("Authentication\nTest login: user\nTest password: 1234");

    ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
    ButtonType signUpButtonType = new ButtonType("Sign up");
    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    dialog.getDialogPane().getButtonTypes().setAll(signUpButtonType, loginButtonType, buttonTypeCancel);

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

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.get() == loginButtonType) {
      try {
        writeMessage(new UserInfo(MessageType.AUTH, username.getText(), hash(password.getText())));
        initDataListener();
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    else if (result.get() == signUpButtonType) {
      System.out.println("SIGN OPTION");
      IONetworkServiceImpl.getService().addHandlerToPipeline(new SignHandler());
      setHandlerOver(true);
    } else {
      IONetworkServiceImpl.getService().disconnect();
    }
  }

}
