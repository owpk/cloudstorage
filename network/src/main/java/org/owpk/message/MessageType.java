package org.owpk.message;

public enum  MessageType {
  OK("ok"),
  DIR("directories request"),
  DOWNLOAD("file download request"),
  UPLOAD("file upload request"),
  CLOSE("close connection"),
  ERROR("server error"),
  DEFAULT("default");
  private final String description;
  MessageType(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description;
  }
}
