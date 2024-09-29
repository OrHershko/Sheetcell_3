package components.versions;

import api.DTO;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import main.AppController;

import java.util.Map;

public class VersionsSelectorComponentController{

    @FXML
    private MenuButton versionsSelectorComponent;

    private AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void updateVersionsSelector() {
        try {
            Map<Integer, DTO> previousVersionsDTO;

            try {
                versionsSelectorComponent.getItems().clear();
                previousVersionsDTO = appController.getSheetsPreviousVersionsDTO();
            }
            catch (Exception e) {
                return;
            }

            for (Map.Entry<Integer, DTO> entry : previousVersionsDTO.entrySet()) {
                int versionNumber = entry.getKey();
                MenuItem menuItem = new MenuItem("Version " + versionNumber);

                menuItem.setOnAction(event -> {
                    appController.loadPreviousVersion(versionNumber);
                });

                versionsSelectorComponent.getItems().add(menuItem);
            }
        } catch (RuntimeException e) {
            AppController.showErrorDialog("Error", e.getMessage());
        }
    }


}
