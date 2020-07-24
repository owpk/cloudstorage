package org.owpk;

import lombok.Data;

import java.io.Serializable;
import java.util.EnumMap;

@Data
public class Messages<T> implements Serializable {
  private static final EnumMap<MessageType, String> activityMap;
  static {
    activityMap = new EnumMap<>(MessageType.class);
  }
  private T payload;
  private MessageType type;

  public Messages(T payload, MessageType type) {
    this.payload = payload;
    this.type = type;
  }

}
