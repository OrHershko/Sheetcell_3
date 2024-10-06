package components.sortandfilter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.AppController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GetRangePopUpController {

    @FXML
    private TextField topLeftTextBox;
    @FXML
    private TextField bottomRightTextBox;
    @FXML
    private Button nextButton;
    @FXML
    private Label operationLabel;

    private Stage currentPopupStage;


    private SortAndFilterController sortAndFilterController;
    private String topLeft;
    private String bottomRight;
    private boolean isSortingOperation; // New flag for sorting or filtering

    public void setSortingOperation(boolean isSorting) {
        this.isSortingOperation = isSorting;
        if (isSortingOperation) {
            operationLabel.setText("Please enter a range to sort:");
        } else {
            operationLabel.setText("Please enter a range to filter:");
        }
    }

    public void setSortAndFilterController(SortAndFilterController sortAndFilterController) {
        this.sortAndFilterController = sortAndFilterController;
    }

    @FXML
    private void nextButtonOnClick() {
        topLeft = topLeftTextBox.getText().toUpperCase();
        bottomRight = bottomRightTextBox.getText().toUpperCase();

        if (topLeft.isEmpty() || bottomRight.isEmpty()) {
            AppController.showErrorDialog("Error","Please enter both top-left and bottom-right cells.");
            return;
        }

        try {
            if(!sortAndFilterController.isRangeValid(topLeft,bottomRight)){
                AppController.showErrorDialog("Error","Make sure the range in valid.");
                return;
            }
            // Open the appropriate dialog based on the operation type (sort or filter)
            if (isSortingOperation) {
                openColumnsDialog(topLeft, bottomRight, "/components/sortandfilter/sort/SortGetColumnsPopUp.fxml", "Sort");
            } else {
                openColumnsDialog(topLeft, bottomRight, "/components/sortandfilter/filter/FilterGetColumnsPopUp.fxml", "Filter");
            }

            Stage stage = (Stage) nextButton.getScene().getWindow();
            stage.close();
        }
        catch (Exception e) {
            AppController.showErrorDialog("Error",e.getMessage());
        }

    }

    private void openColumnsDialog(String topLeft, String bottomRight, String componentFilePath, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(componentFilePath));
            Parent root = fxmlLoader.load();

            ColumnActionController controller = fxmlLoader.getController();
            controller.setGetRangePopUpController(this);
            controller.setChoiceBox(createChoiceBox(topLeft, bottomRight));

            Stage stage = new Stage();
            controller.setPopUpStage(stage);
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            if(currentPopupStage != null) {
                currentPopupStage.close();
            }
            stage.show();
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChoiceBox<String> createChoiceBox(String topLeft, String bottomRight) {

        ChoiceBox<String> columnsChoiceBox = new ChoiceBox<>();

        String startColumn = topLeft.replaceAll("\\d", "");
        String endColumn = bottomRight.replaceAll("\\d", "");

        List<String> columnRange = getColumnRange(startColumn, endColumn);

        columnsChoiceBox.getItems().clear();
        for (String column : columnRange) {
            columnsChoiceBox.getItems().add("Column " + column); // Add "Column" before each column letter
        }
        columnsChoiceBox.setValue("Choose Column");

        return columnsChoiceBox;
    }

    private List<String> getColumnRange(String start, String end) {
        List<String> columns = new ArrayList<>();

        int startChar = start.charAt(0);
        int endChar = end.charAt(0);

        for (int i = startChar; i <= endChar; i++) {
            columns.add(String.valueOf((char) i));
        }
        return columns;
    }


    public void sort(List<String> columnToSortBy) throws IOException {
        sortAndFilterController.sort(columnToSortBy, topLeft, bottomRight);
    }

    public Set<String> getValuesFromColumn(String column) throws IOException {
        return sortAndFilterController.getValuesFromColumn(column, topLeft, bottomRight);
    }

    public void filter(Map<String, Set<String>> colToSelectedValues) throws IOException {
        sortAndFilterController.filter(colToSelectedValues, topLeft, bottomRight);
    }

    public void setPopUpStage(Stage stage) {
        currentPopupStage = stage;
    }
}
