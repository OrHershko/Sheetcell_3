package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.http.HttpClientUtil;

import java.io.IOException;
import java.net.URL;

import static utils.Constants.*;

public class SheetcellClientMain extends Application {

    @Override
    public void start(Stage primaryStage) {

        URL loginPage = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPage);
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Sheetcell");
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop(){
        HttpClientUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
