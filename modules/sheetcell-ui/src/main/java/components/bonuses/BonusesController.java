package components.bonuses;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import main.AppController;

public class BonusesController {

    private AppController appController;
    public static BooleanProperty animationsEnabledProperty = new SimpleBooleanProperty(false);

    @FXML
    private MenuButton skinsMenuButton;

    @FXML
    private MenuButton animationsMenuButton;

    @FXML
    private MenuItem defaultSkinMenuItem;

    @FXML
    private MenuItem darkSkinMenuItem;

    @FXML
    private MenuItem lightSkinMenuItem;

    @FXML
    private MenuItem animationsOnMenuItem;

    @FXML
    private MenuItem animationsOffMenuItem;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    public void initialize() {
        defaultSkinMenuItem.setOnAction(event -> appController.applySkin("default"));
        darkSkinMenuItem.setOnAction(event -> appController.applySkin("dark"));
        lightSkinMenuItem.setOnAction(event -> appController.applySkin("light"));

        animationsOnMenuItem.setOnAction(event -> animationsEnabledProperty.set(true));
        animationsOffMenuItem.setOnAction(event -> animationsEnabledProperty.set(false));
    }
}
