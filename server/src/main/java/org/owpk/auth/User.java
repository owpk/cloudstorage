package org.owpk.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
  @Id
  private int id;
  private String userFolder;
  private int passwordHash;
  private String userName;
  private String email;
}
