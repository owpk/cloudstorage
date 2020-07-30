package org.owpk.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.owpk.util.FileInfo;

public class ClientPanel {

  private TableView<FileInfo> client_panel;

  public ClientPanel(TableView<FileInfo> client_panel) {
    this.client_panel = client_panel;
  }

  private void initListeners() {

  }
}
