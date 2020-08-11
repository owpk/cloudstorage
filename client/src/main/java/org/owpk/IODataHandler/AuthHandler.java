package org.owpk.IODataHandler;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@Getter
@Setter
public class AuthHandler extends AbsHandler {
  private final Logger log = LogManager.getLogger(AuthHandler.class.getName());
  private final CountDownLatch doneLatch = new CountDownLatch(1);
  private boolean signRequest;
  private String login;
  private String pass;

  private String hash(String input) {
    return DigestUtils.sha256Hex(input);
  }

  public void showDialog() {
    Platform.runLater(()-> {
      loginDialog();
      doneLatch.countDown();
    });
  }

  public boolean tryToAuth() throws IOException, ClassNotFoundException, InterruptedException, AuthException {
    doneLatch.await();
    if (signRequest) return true;
    else if (login != null && pass != null) {
      writeMessage(new UserInfo(MessageType.AUTH, login, hash(pass)));
      initDataListener();
      return false;
    } else throw new AuthException("Login and password should not be empty");
  }

  @Override
  protected void listen(Message<?> msg) throws AuthenticationException {
    if (msg.getType() == MessageType.OK) {
      handlerIsOver = true;
    } else if (msg.getType() == MessageType.ERROR) {
      throw new AuthenticationException((String) msg.getPayload());
    }
  }

  private void loginDialog() {
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Login");
    dialog.setHeaderText("Authentication\nTest login: user\nTest password: 1234");

    ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
    ButtonType signButtonType = new ButtonType("Sign", ButtonBar.ButtonData.OTHER);
    dialog.getDialogPane().getButtonTypes().addAll(signButtonType, loginButtonType, ButtonType.CANCEL);

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
      } else if (dialogButton == signButtonType) {
        handlerIsOver = true;
        signRequest = true;
      }
      return null;
    });
    Optional<Pair<String, String>> result = dialog.showAndWait();
    result.ifPresent(usernamePassword -> {
      login = usernamePassword.getKey();
      pass = usernamePassword.getValue();
    });
  }

}
