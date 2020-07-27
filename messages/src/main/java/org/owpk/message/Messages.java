package org.owpk.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class Messages<T> implements Serializable {

  private T payload;
  private MessageType type;

  public Messages(MessageType type, T payload) {
    this.payload = payload;
    this.type = type;
  }

}
