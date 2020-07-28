package org.owpk.network;

/**
 * стандартные команды
 */
public enum Commands {
  DOWNLOAD("./download"),
  UPLOAD("./upload"),
  DIR("./dir"),
  OK("./ok"),
  CLOSE("./close"),
  ERROR("./err");

  private String cmd;
  Commands(String cmd) {
    this.cmd = cmd;
  }

  public String getCmd() {
    return cmd;
  }
}
