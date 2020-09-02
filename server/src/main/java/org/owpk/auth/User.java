package org.owpk.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;
  private String login;
  private String password_hash;
  private String server_folder;
  private String email;

  public User(String login, String password_hash, String server_folder, String email) {
    this.login = login;
    this.password_hash = password_hash;
    this.server_folder = server_folder;
    this.email = email;
  }
}


