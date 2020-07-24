package org.owpk.message;

import lombok.Data;

import java.io.Serializable;
import java.util.EnumMap;

@Data
public class Messages implements Serializable {
  private static final EnumMap<MessageType, String> typeMap;
  static {
    typeMap = new EnumMap<>(MessageType.class);
  }
  private Object payload;
  private MessageType type;

  public Messages(Object payload, MessageType type) {
    this.payload = payload;
    this.type = type;
  }

  public static EnumMap<MessageType, String> getTypeMap() {
    return typeMap;
  }
}
