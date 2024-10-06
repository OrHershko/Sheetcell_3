package components.sortandfilter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import main.AppController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SortAndFilterController {

    @FXML
    private Button sortButton;

    @FXML
    private Button filterButton;

    private AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void sortOnClick() {
        try {
            openGetRangeDialog(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGetRangeDialog(boolean isSorting) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/components/sortandfilter/GetRangePopUp.fxml"));
        Parent root = fxmlLoader.load();

        GetRangePopUpController controller = fxmlLoader.getController();
        controller.setSortingOperation(isSorting);
        controller.setSortAndFilterController(this);

        Stage stage = new Stage();
        controller.setPopUpStage(stage);
        if(isSorting) {
            stage.setTitle("Sort Range");
        } else {
            stage.setTitle("Filter Range");
        }
        stage.setScene(new Scene(root, 400, 200));
        stage.show();
    }

    @FXML
    private void filterOnClick() {
        try {
            openGetRangeDialog(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRangeValid(String topLeft, String bottomRight) {
        try{
            return appController.checkRangeOfCells(topLeft,bottomRight);
        }
        catch (Exception e) {
            throw new RuntimeException("Error: Please enter a cell in the format A1, B2, etc.");
        }
    }

    public void sort(List<String> columnToSortBy, String topLeft, String bottomRight) throws IOException {
        appController.sortSheetByColumns(columnToSortBy, topLeft, bottomRight);
    }

    public Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight) throws IOException {
        return appController.getValuesFromColumn(column,topLeft,bottomRight);
    }

    public void filter(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight) throws IOException {
        appController.filter(colToSelectedValues,topLeft,bottomRight);
    }
}
