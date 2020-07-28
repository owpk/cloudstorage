package org.owpk.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.owpk.controller.Controller;

public class Client extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setResizable(true);

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_scene.fxml"));
    Parent root = loader.load();

    //убираем стандартные рамки окна ОС
    primaryStage.initStyle(StageStyle.UNDECORATED);

    //передаем контроллеру stage, чтобы можно было перемещать окно мышью
    Controller controller = loader.getController();



    primaryStage.setScene(new Scene(root, 1300, 700));
    //инициализируем слушателей событий
    controller.setStageAndSetupListeners(primaryStage);
    primaryStage.show();
  }


}
