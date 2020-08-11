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
import org.owpk.network.IONetworkServiceImpl;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Getter
@Setter
public class AuthHandler extends AbsHandler {
  private final Logger log = LogManager.getLogger(AuthHandler.class.getName());
  private final CountDownLatch doneLatch = new CountDownLatch(1);

  private String hash(String input) {
    return DigestUtils.sha256Hex(input);
  }

  private void showDialog() throws AuthException {
    Platform.runLater(() -> {
      loginDialog();
      doneLatch.countDown();
    });
  }

  @Override
  protected void listen(Message<?> msg) {
    if (msg.getType() == MessageType.OK) {
      handlerIsOver = true;
      IONetworkServiceImpl.getService().addMainDataHandler();
    } else if (msg.getType() == MessageType.ERROR) {
      throw new AuthException((String) msg.getPayload());
    }
  }

  @Override
  public void execute() throws InterruptedException, AuthException, IOException, ClassNotFoundException {
    showDialog();
    doneLatch.await();
  }

  private void loginDialog() {
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Login");
    dialog.setHeaderText("Authentication\nTest login: user\nTest password: 1234");

    ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
    ButtonType signButtonType = new ButtonType("Sign up", ButtonBar.ButtonData.OTHER);

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
//    dialog.setOnCloseRequest(x -> IONetworkServiceImpl.getService().disconnect());

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        try {
          writeMessage(new UserInfo(MessageType.AUTH, username.getText(), hash(password.getText())));
          initDataListener();
        } catch (IOException | ClassNotFoundException e) {
          e.printStackTrace();
        }
      } else if (dialogButton == signButtonType) {
        IONetworkServiceImpl.getService().addHandlerToPipeline(new SignHandler());
        this.handlerIsOver = true;
      }
//      else if (dialogButton == ButtonType.CANCEL) {
//        IONetworkServiceImpl.getService().disconnect();
//      }
      return null;
    });
    dialog.showAndWait();
  }

}
