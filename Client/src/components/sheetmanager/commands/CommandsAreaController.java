package components.sheetmanager.commands;

import components.sheetmanager.SheetManagerController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import main.AppController;
import utils.Permissions;

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

        Permissions permission = Permissions.valueOf(sheetManagerController.getPermissionTypeForUser(sheetManagerController.getUsername()));

        if (permission != Permissions.NO_PERMISSION) {
            sheetManagerController.startMainApp(permission);
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
        if(sheetManagerController.getPermissionTypeForUser(sheetManagerController.getUsername()).equals("OWNER"))
        {
            sheetManagerController.showPermissionRequestResponsePopUp();
        }
        else
        {
            AppController.showErrorDialog("Error","You don't have permission to view this page.");

        }

    }

}
