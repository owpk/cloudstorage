package org.owpk.IODataHandler;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.owpk.controller.UserDialog;
import org.owpk.message.Message;
import org.owpk.message.MessageType;
import org.owpk.message.UserInfo;
import org.owpk.network.NetworkServiceInt;

import java.io.IOException;
import java.util.Optional;

@Getter
@Setter
public class AuthHandler {
  private String login;
  private String password;

  private String hash(String input) {
    return DigestUtils.sha256Hex(input);
  }

  public boolean tryToAuth() {
    Optional<Pair<String, String>> result = UserDialog.loginDialog();
    result.ifPresent(usernamePassword -> {
      login = usernamePassword.getKey();
      password = usernamePassword.getValue();
    });
    if (result.isPresent()) {
      try {
        NetworkServiceInt serviceInt = IONetworkServiceImpl.getService();
        ObjectDecoderInputStream in = (ObjectDecoderInputStream) serviceInt.getIn();
        ((ObjectEncoderOutputStream) IONetworkServiceImpl
            .getService()
            .getOut())
            .writeObject(new UserInfo(MessageType.AUTH, login, hash(password)));
        Message<?> msg;
        while (true) {
          if (in.available() > 0) {
            msg = (Message<?>) in.readObject();
            if (msg.getType() == MessageType.OK)
              return false;
            if (msg.getType() == MessageType.ERROR) {
              String cause = (String) msg.getPayload();
              UserDialog.errorDialog(cause);
              return true;
            }
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    return true;
  }
}
