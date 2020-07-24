package org.owpk;

public enum  MessageType {
  OK("$ok"),
  DIR("$dir"),
  DOWNLOAD("$download"),
  UPLOAD("$upload");

  private String cmd;

  MessageType(String cmd) {
    this.cmd = cmd;
  }

  public String getCmd() {
    return cmd;
  }
}
