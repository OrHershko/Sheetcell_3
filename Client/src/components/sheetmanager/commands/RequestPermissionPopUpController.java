package components.sheetmanager.commands;

import components.sheetmanager.SheetManagerController;
import impl.sheet.SheetData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class RequestPermissionPopUpController {

    @FXML
    private Label selectedSheetLabel;

    @FXML
    private MenuButton requestTypeMenuButton;

    @FXML
    private Button createRequestButton;

    private SheetManagerController sheetManagerController;

    private SheetData selectedSheet;

    @FXML
    public void initialize() {

        requestTypeMenuButton.getItems().add(new MenuItem("Reader"));
        requestTypeMenuButton.getItems().add(new MenuItem("Writer"));

        for (MenuItem item : requestTypeMenuButton.getItems()) {
            item.setOnAction(this::handleMenuItemAction);
        }
    }

    @FXML
    private void handleMenuItemAction(ActionEvent event) {
        MenuItem selectedItem = (MenuItem) event.getSource();
        requestTypeMenuButton.setText(selectedItem.getText()); // Set the text of the MenuButton to the selected item
    }


    @FXML
    private void CreateRequestOnClick() throws IOException {
        String requestType = requestTypeMenuButton.getText();
        sheetManagerController.createPermissionRequest(selectedSheet, requestType);
        requestTypeMenuButton.getScene().getWindow().hide();
    }

    public void setSheetManagerController(SheetManagerController sheetManagerController) {
        this.sheetManagerController = sheetManagerController;
    }

    public void setSheetData(SheetData selectedSheet) {
        this.selectedSheet = selectedSheet;
        selectedSheetLabel.setText(selectedSheetLabel.getText() + selectedSheet.getSheetName());
    }
}
