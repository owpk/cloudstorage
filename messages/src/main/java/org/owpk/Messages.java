package org.owpk;

import lombok.Data;

import java.io.Serializable;

@Data
public class Messages<T> implements Serializable {
  private T payload;
  private MessageType type;

  public Messages(T payload, MessageType type) {
    this.payload = payload;
    this.type = type;
  }

}
