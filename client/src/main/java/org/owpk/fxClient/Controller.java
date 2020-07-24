package org.owpk.fxClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import org.owpk.command.AbsCommandHandler;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

  @FXML private ListView<String> lv;
  @FXML private Button send;
  @FXML private TextField txt;
  private AbsCommandHandler ch;

  public void sendCommand(ActionEvent actionEvent) throws IOException {
    String command = txt.getText();
    ch.listen(command);
  }

  @SneakyThrows
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Network network = new Network();
    ch = new ClientCommandHandler(network);
  }
}
