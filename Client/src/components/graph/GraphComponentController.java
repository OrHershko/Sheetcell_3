package components.graph;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import main.AppController;

import java.io.IOException;

public class GraphComponentController {

    @FXML
    private Button createGraphButton;

    private AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }


    @FXML
    private void createGraphOnClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/components/graph/GetGraphRangePopUp.fxml"));
        Parent root = fxmlLoader.load();
        GetGraphRangePopUpController controller = fxmlLoader.getController();
        controller.setGraphComponentController(this);
        controller.setColumnsChoiceBoxes();

        Stage stage = new Stage();
        controller.setCurrentPopupStage(stage);
        stage.setTitle("Build Graph");
        stage.setScene(new Scene(root, 480, 150));
        stage.show();
    }

    public double getValueFromCell(int rowX, String xColumn) {
        return appController.getCellValue(rowX, xColumn);
    }

    public int getNumOfColumns() throws IOException {
        return appController.getNumOfColumnsInGrid();
    }
}
