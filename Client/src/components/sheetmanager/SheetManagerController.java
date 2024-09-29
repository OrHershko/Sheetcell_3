package components.sheetmanager;

import components.sheetmanager.commands.CommandsAreaController;
import components.sheetmanager.tables.TablesAreaController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.AppController;

import java.io.IOException;
import java.net.URL;

import static utils.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;

public class SheetManagerController {

    private AppController appController;

    private String username;

    @FXML
    private VBox tablesArea;

    @FXML
    private TablesAreaController tablesAreaController;

    @FXML
    private AnchorPane commandsArea;

    @FXML
    private CommandsAreaController commandsAreaController;

    public void initialize() {
        tablesAreaController.setSheetManagerController(this);
        commandsAreaController.setSheetManagerController(this);
    }

    public void startMainApp() {
        URL appPage = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        Platform.runLater(() -> {
            try {
                Stage primaryStage = (Stage) tablesArea.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(appPage);
                Parent root = fxmlLoader.load();
                appController = fxmlLoader.getController();
                appController.setNewSelectedSheet(tablesAreaController.getSelectedSheet());
                Scene scene = new Scene(root,1200,800);
                appController.applySkin("default");
                primaryStage.setTitle("Sheetcell");
                primaryStage.setScene(scene);
                primaryStage.centerOnScreen();
                primaryStage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
                AppController.showErrorDialog("Failed to load app", e.getMessage());
            }
        });
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
