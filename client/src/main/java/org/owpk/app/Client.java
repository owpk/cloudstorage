package org.owpk.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.owpk.controller.MainSceneController;

public class Client extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setResizable(true);

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_scene.fxml"));
    Parent root = loader.load();

    //убираем стандартные рамки окна ОС
    primaryStage.initStyle(StageStyle.TRANSPARENT);

    //передаем контроллеру stage, чтобы можно было перемещать окно мышью
    MainSceneController mainSceneController = loader.getController();

    primaryStage.setScene(new Scene(root, 1500, 850));
    //инициализируем слушателей событий
    mainSceneController.initWindowControls(primaryStage);
    primaryStage.show();
  }

}
