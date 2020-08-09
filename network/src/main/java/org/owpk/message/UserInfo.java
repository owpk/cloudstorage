package org.owpk.message;

import lombok.Data;

@Data
public class UserInfo extends Message<String> {
  private String login;
  private String password;

  public UserInfo(MessageType type, String login, String password) {
    super(type);
    this.login = login;
    this.password = password;
  }

}
