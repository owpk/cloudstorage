package org.owpk.message;

import lombok.Data;

@Data
public class UserInfo extends Message<String> {
  private String login;
  private String password;
  private String email;

  public UserInfo(MessageType type, String login, String password) {
    super(type);
    this.login = login;
    this.password = password;
  }

  public UserInfo(MessageType type, String login, String password, String email) {
    super(type);
    this.login = login;
    this.password = password;
    this.email = email;
  }
}
