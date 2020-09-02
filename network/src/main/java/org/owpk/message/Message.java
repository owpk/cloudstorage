package org.owpk.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message<T> implements Serializable {

  private T payload;
  protected MessageType type;

  public Message(MessageType type, T payload) {
    this.payload = payload;
    this.type = type;
  }

  public Message(MessageType type) {
    this.type = type;
  }

}
