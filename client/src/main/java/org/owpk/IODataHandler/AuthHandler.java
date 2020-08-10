package org.owpk.IODataHandler;

import javafx.application.Platform;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owpk.controller.UserDialog;
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
  private String login;
  private String password;

  private String hash(String input) {
    return DigestUtils.sha256Hex(input);
  }

  public void showDialog() {
    Platform.runLater(() -> {
      Optional<Pair<String, String>> result = UserDialog.loginDialog();
      result.ifPresent(usernamePassword -> {
        login = usernamePassword.getKey();
        password = usernamePassword.getValue();
      });
      doneLatch.countDown();
    });
  }

  public void tryToAuth() throws IOException, ClassNotFoundException, InterruptedException, AuthException {
    doneLatch.await();
    if (login != null && password != null) {
      writeMessage(new UserInfo(MessageType.AUTH, login, hash(password)));
      initDataListener();
    } else
      throw new AuthException("Login and password should not be empty");
  }

  @Override
  protected void listen(Message<?> msg) throws AuthenticationException {
    if (msg.getType() == MessageType.OK) {
      log.info("OK");
      handlerIsOver = true;
    } else if (msg.getType() == MessageType.ERROR) {
      throw new AuthenticationException((String) msg.getPayload());
    }
  }
}
