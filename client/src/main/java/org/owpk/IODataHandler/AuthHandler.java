package org.owpk.IODataHandler;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
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
public class AuthHandler {
  private final Logger log = LogManager.getLogger(AuthHandler.class.getName());
  private String login;
  private String password;

  private String hash(String input) {
    return DigestUtils.sha256Hex(input);
  }
  final CountDownLatch doneLatch = new CountDownLatch(1);

  public void showDialog() {
    Platform.runLater(() -> {
      Optional<Pair<String, String>> result = UserDialog.loginDialog();
      result.ifPresent(usernamePassword -> {
        login = usernamePassword.getKey();
        password = usernamePassword.getValue();
        doneLatch.countDown();
    });
    });
  }

  private void initDataListener() throws IOException, ClassNotFoundException, AuthException {
    log.info("Auth data listener started");
    ObjectDecoderInputStream in = (ObjectDecoderInputStream) IONetworkServiceImpl.getService().getIn();
    Message<?> msg;
    while (true) {
      if (in.available() > 0) {
        msg = (Message<?>) in.readObject();
        if (msg.getType() == MessageType.OK)
          return;
        else if (msg.getType() == MessageType.ERROR) {
          throw new AuthenticationException((String) msg.getPayload());
        }
      }
    }
  }

  public void tryToAuth() throws IOException, ClassNotFoundException, InterruptedException, AuthException {
    doneLatch.await();
      if (login != null && password != null) {
        System.out.println(login + ":"+ password);
        ((ObjectEncoderOutputStream) IONetworkServiceImpl
            .getService()
            .getOut())
            .writeObject(new UserInfo(MessageType.AUTH, login, hash(password)));
        initDataListener();
      }
    throw new AuthException("Login and password should not be empty");
  }
}
