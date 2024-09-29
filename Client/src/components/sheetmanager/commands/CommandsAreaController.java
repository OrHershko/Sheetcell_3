package components.sheetmanager.commands;

import components.sheetmanager.SheetManagerController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CommandsAreaController {

    @FXML
    private Button viewSheetButton;

    private SheetManagerController sheetManagerController;

    public void setSheetManagerController(SheetManagerController sheetManagerController) {
        this.sheetManagerController = sheetManagerController;
    }

    @FXML
    private void viewSheetOnClick() {
        sheetManagerController.startMainApp();
    }
}
