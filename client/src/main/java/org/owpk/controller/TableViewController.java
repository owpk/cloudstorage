package org.owpk.controller;

import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
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
 * Controller {@link TableView}, creates a table that stores data of the {@link FileInfo} type,
 * calculates the size for files and last modified date
 */
public class TableViewController implements Initializable {
  @FXML public TableView<FileInfo> table;
  private TableColumn<FileInfo, FileInfo.FileType> client_column_file_type;
  private TableColumn<FileInfo, String> client_column_file_name;
  private TableColumn<FileInfo, Long> client_column_file_size;
  private TableColumn<FileInfo, String> client_column_last_changed;

  /**
   * Type column
   * Creates an icon for a cell in a table depending on the file type
   */
  private static class TypeImageCellStylist<T> extends TableCell<T, FileInfo.FileType> {
    private ImageView image;

    public TypeImageCellStylist() {
      image = new IconBuilder().build();
      setGraphic(image);
      setMinHeight(20);
    }

    @Override
    protected void updateItem(FileInfo.FileType type, boolean empty) {
      super.updateItem(type, empty);
      if (empty || type == null)
        image.setImage(null);
      else {
        image.setImage(IconBuilder.getFileTypeIconMap().get(type));
      }
    }
  }

  /**
   * Size column
   * Calculate file size units {@link #computeSize (Long)}
   * creates a cell in a table
   */
  private static class SizeCellStylist extends TableCell<FileInfo, Long> {
    private final static String[] SIZES = {"B","KB","MB","GB","TB"};
    private int counter;

    @Override
    protected void updateItem(Long item, boolean empty) {
      super.updateItem(item, empty);
      if (item == null || empty) {
        setText(null);
        setStyle("");
      } else {
        String text;
        if (item == -1) text = "";
        else text = computeSize(item);
        setText(text);
      }
    }

    private String computeSize(Long bytes) {
      if (bytes > 1024) {
        bytes /= 1024;
        if (counter < SIZES.length)
          counter++;
        return computeSize(bytes);
      } else {
        String s = bytes + " " + SIZES[counter];
        counter = 0;
        return s;
      }
    }
  }

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
