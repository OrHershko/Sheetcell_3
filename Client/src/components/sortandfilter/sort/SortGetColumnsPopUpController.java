package components.sortandfilter.sort;

import components.sortandfilter.ColumnActionController;
import components.sortandfilter.GetRangePopUpController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.AppController;

import java.util.ArrayList;
import java.util.List;

public class SortGetColumnsPopUpController implements ColumnActionController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox sortGetColumnsPopUp;

    @FXML
    private ChoiceBox<String> columnsChoiceBox;

    @FXML
    private Button addSortColumnButton;

    @FXML
    private Button sortButton;

    private Stage currentPopupStage;

    private GetRangePopUpController getRangePopUpController;

    private List<ChoiceBox<String>> addedColumns = new ArrayList<>();


    @FXML
    private void addColumnToSortOnClick() {

        ChoiceBox<String> newChoiceBox = new ChoiceBox<>();
        addedColumns.add(newChoiceBox);
        newChoiceBox.getItems().addAll(columnsChoiceBox.getItems());

        HBox newHBox = new HBox(10);
        Label newLabel = new Label("Then by:");
        newHBox.getChildren().addAll(newLabel, newChoiceBox);
        Insets padding = new Insets(10.0, 10.0, 10.0, 10.0);
        newHBox.setPadding(padding);

        sortGetColumnsPopUp.getChildren().add(sortGetColumnsPopUp.getChildren().size() - 2, newHBox); // הוספתו לפני הכפתור "Sort"
        Stage stage = (Stage) scrollPane.getScene().getWindow();
        stage.sizeToScene();
    }

    @FXML
    private void sortOnClick(){
        List<String> columnToSortBy = new ArrayList<>();
        columnToSortBy.add(columnsChoiceBox.getValue());
        for (ChoiceBox<String> column : addedColumns) {
            columnToSortBy.add(column.getValue());
        }

        try {
            getRangePopUpController.sort(columnToSortBy);
        }
        catch (Exception e) {
            AppController.showErrorDialog("Error", "Make sure the range provided contains only numeric values.");
        }

        if(currentPopupStage != null){
            currentPopupStage.close();
        }
    }

    @Override
    public void setChoiceBox(ChoiceBox<String> choiceBox) {
        this.columnsChoiceBox.setItems(choiceBox.getItems());
    }

    @Override
    public void setGetRangePopUpController(GetRangePopUpController getRangePopUpController) {
        this.getRangePopUpController = getRangePopUpController;
    }

    @Override
    public void setPopUpStage(Stage stage) {
        currentPopupStage = stage;
    }
}
