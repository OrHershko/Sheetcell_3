package components.sheetmanager.commands;

import components.sheetmanager.SheetManagerController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CommandsAreaController {

    @FXML
    private Button viewSheetButton;

    @FXML
    private Button createPermissionRequestButton;

    @FXML
    private Button responseToPermissionRequestsButton;

    private SheetManagerController sheetManagerController;

    public void setSheetManagerController(SheetManagerController sheetManagerController) {
        this.sheetManagerController = sheetManagerController;
    }

    @FXML
    private void viewSheetOnClick() {

        if(sheetManagerController.isPermittedToViewSheet()) {
            sheetManagerController.startMainApp();
        } else {
            sheetManagerController.showPermissionErrorDialog();
        }

    }

    @FXML
    private void createPermissionRequestOnClick() {
        sheetManagerController.showRequestPermissionPopUp();
    }

    @FXML
    private void responseToPermissionRequestsOnClick() {
        sheetManagerController.showPermissionRequestResponsePopUp();
    }

}
