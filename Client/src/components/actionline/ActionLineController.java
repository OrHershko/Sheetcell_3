package components.actionline;

import dto.CellDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.AppController;

import java.io.IOException;

public class ActionLineController {

    @FXML
    private Label selectedCellId;

    @FXML
    private Label originalCellValue;

    @FXML
    private Label lastUpdateCellVersion;

    @FXML
    private TextField textField;

    @FXML
    private Button updateCellButton;

    @FXML
    private Button dynamicCalculationButton;

    private String currentCellId;

    private AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void initialize() {
        updateCellButton.setDisable(true);
        textField.setDisable(true);
        dynamicCalculationButton.setDisable(true);
    }


    public void displayCellData(CellDTO cell) {
        currentCellId = cell.getIdentity();
        selectedCellId.setText("Selected Cell ID: " + currentCellId);
        originalCellValue.setText("Original Cell Value: " + cell.getOriginalValue());
        if(cell.getVersion() != 0)
        {
            lastUpdateCellVersion.setText("Last Update Cell Version: " + cell.getVersion());
        }
        else {
            lastUpdateCellVersion.setText("Last Update Cell Version: ");
        }
        updateCellButton.setDisable(false);
        textField.setDisable(false);
        dynamicCalculationButton.setDisable(!appController.isCellValueNumeric(currentCellId));
    }

    @FXML
    public void updateCell(){
        try {
            appController.updateCellDataToEngine(currentCellId ,textField.getText());
        }
        catch (Exception e) {
            AppController.showErrorDialog("Update Cell Error", e.getMessage());
        }
    }

    @FXML
    private void dynamicCalculationOnClick() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/components/actionline/GetSliderSettingsPopUp.fxml"));
        Parent root = fxmlLoader.load();

        GetSliderSettingsPopUpController controller = fxmlLoader.getController();
        controller.setActionLineController(this);

        Stage stage = new Stage();
        controller.setCurrentPopupStage(stage);
        stage.setTitle("Enter Range");
        stage.setScene(new Scene(root, 250, 200));
        stage.show();
    }

    public void updateCellDataToEngine(String newValue) throws IOException {
        appController.showDynamicCalculation(currentCellId, newValue);
    }

    public void showOriginalGrid() throws IOException {
        appController.showCurrentSheetOnGrid();
    }
}
