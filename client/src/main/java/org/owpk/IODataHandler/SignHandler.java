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

@Getter
@Setter
public class SignHandler extends AbsHandler{
  private final CountDownLatch doneLatch = new CountDownLatch(1);
  private String login;
  private String password;
  private String email;

  private void showDialog() {
      signDialog();
  }


  @Override
  protected void listen(Message<?> message) throws IOException {
    switch (message.getType()) {
      case OK:
        handlerIsOver = true;
        doneLatch.countDown();
        IONetworkServiceImpl.getService().addHandlerToPipeline(new AuthHandler());
        break;
      case ERROR:
        Platform.runLater(() -> {
          UserDialog.errorDialog(message.getPayload().toString());
          showDialog();
        });

        break;
    }
  }

  @Override
  public void execute() throws InterruptedException {
    System.out.println("EXECUTE");
    Platform.runLater(()-> {
      showDialog();
      try {
        writeMessage(new UserInfo(MessageType.SIGN, "login", "password", "email"));
        initDataListener();
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });
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

    Node loginButton = dialog.getDialogPane().lookupButton(signButtonType);
    loginButton.setDisable(true);

    username.textProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue.trim().isEmpty()));

    dialog.getDialogPane().setContent(grid);

    Platform.runLater(username::requestFocus);
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == signButtonType) {
        this.login = username.getText();
        this.password = password.getText();
        this.email = email.getText();
      } else {
        handlerIsOver = true;
      }
      return null;
    });
    dialog.showAndWait();
  }
}
