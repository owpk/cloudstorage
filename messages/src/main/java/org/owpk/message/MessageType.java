package org.owpk.message;

public enum  MessageType {
  OK("$ok"),
  DIR("$dir"),
  DOWNLOAD("$download"),
  UPLOAD("$upload"),
  CLOSE("$close");

  private String cmd;

  MessageType(String cmd) {
    this.cmd = cmd;
  }

  public String getCmd() {
    return cmd;
  }
}
