package org.owpk.controller;

import org.owpk.util.FileInfo;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Класс инициализирующий колонки для таблиц
 */
public class TableController implements Initializable {
  @FXML public TableView<FileInfo> table;
  public TableColumn<FileInfo, FileInfo.FileType> client_column_file_type;
  public TableColumn<FileInfo, String> client_column_file_name;
  public TableColumn<FileInfo, Long> client_column_file_size;
  public TableColumn<FileInfo, String> client_column_last_changed;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    client_column_file_type = new TableColumn<>("Type");
    client_column_file_name = new TableColumn<>("Name");
    client_column_file_size = new TableColumn<>("Size");
    client_column_last_changed = new TableColumn<>("Changed");

    client_column_file_type.setCellValueFactory(new PropertyValueFactory<>("fileType"));
    client_column_file_type.setCellFactory(p -> new TypeImageCellStylist<>());
    client_column_file_type.setPrefWidth(40);
    client_column_file_type.setResizable(false);

    client_column_file_name.setCellValueFactory(p -> new SimpleStringProperty(
        p.getValue().getFilename()));
    client_column_file_name.setPrefWidth(230);

    client_column_file_size.setCellValueFactory(p -> new SimpleObjectProperty<>(
        p.getValue().getSize()));
    client_column_file_size.setCellFactory(p -> new SizeCellStylist());
    client_column_file_size.setPrefWidth(110);

    DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    client_column_last_changed.setCellValueFactory(p -> new SimpleStringProperty(
        p.getValue().getLastModified().format(format)));

    table.getColumns().add(client_column_file_type);
    table.getColumns().add(client_column_file_name);
    table.getColumns().add(client_column_file_size);
    table.getColumns().add(client_column_last_changed);
    client_column_file_type.setSortType(TableColumn.SortType.DESCENDING);
    table.getSortOrder().add(client_column_file_type);
  }

}
