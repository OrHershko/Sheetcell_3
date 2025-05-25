package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/App.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1150, 600);
        AppController controller = fxmlLoader.getController();
        controller.applySkin("default");

        primaryStage.setTitle("Shticell");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
