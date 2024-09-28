package components.sortandfilter.filter;

import components.sortandfilter.ColumnActionController;
import components.sortandfilter.GetRangePopUpController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class FilterGetColumnsPopUpController implements ColumnActionController {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox filterGetColumnsPopUp;

    @FXML
    private ChoiceBox<String> columnsChoiceBox;

    @FXML
    private Button addFilterColumnButton;

    @FXML
    private Button filterButton;

    private Stage currentPopupStage;


    private GetRangePopUpController getRangePopUpController;

    private List<ChoiceBox<String>> addedColumns = new ArrayList<>();

    @FXML
    private ListView<String> valuesListView;

    private List<ListView<String>> addedValues = new ArrayList<>();

    @FXML
    void initialize() {
        valuesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }


    @Override
    public void setPopUpStage(Stage stage) {
        currentPopupStage = stage;
    }

    @Override
    public void setGetRangePopUpController(GetRangePopUpController getRangePopUpController) {
        this.getRangePopUpController = getRangePopUpController;
    }


    @Override
    public void setChoiceBox(ChoiceBox<String> choiceBox) {
        this.columnsChoiceBox.setItems(choiceBox.getItems());
        this.columnsChoiceBox.setValue("Column");

        addListenerToColumnsChoiceBox(columnsChoiceBox, valuesListView);
    }

    private void addListenerToColumnsChoiceBox(ChoiceBox<String> columns , ListView<String> values) {
        columns.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Set<String> valuesInCol = getRangePopUpController.getValuesFromColumn(newValue.replace("Column ", "").trim());
                values.getItems().clear();
                values.getItems().addAll(valuesInCol);
            }
        });
    }

    @FXML
    private void addColumnToFilterOnClick() {

        ChoiceBox<String> newChoiceBox = new ChoiceBox<>();
        newChoiceBox.setValue("Column");
        addedColumns.add(newChoiceBox);
        newChoiceBox.getItems().addAll(columnsChoiceBox.getItems());
        ListView<String> newValueListView = new ListView<>();
        newValueListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        newValueListView.setMinWidth(Region.USE_PREF_SIZE);
        newValueListView.setPrefWidth(200);
        newValueListView.setMinHeight(Region.USE_PREF_SIZE);
        newValueListView.setPrefHeight(100);
        addedValues.add(newValueListView);
        valuesListView.minWidthProperty().bind(newChoiceBox.widthProperty());
        addListenerToColumnsChoiceBox(newChoiceBox, newValueListView);

        HBox newHBox = new HBox(10);
        Label newLabel = new Label("Then by:");
        newHBox.getChildren().addAll(newLabel, newChoiceBox, newValueListView);
        Insets padding = new Insets(10.0, 10.0, 10.0, 10.0);
        newHBox.setPadding(padding);

        filterGetColumnsPopUp.getChildren().add(filterGetColumnsPopUp.getChildren().size() - 2, newHBox);
        Stage stage = (Stage) scrollPane.getScene().getWindow();
        stage.sizeToScene();
    }

    @FXML
    private void filterOnClick(){
        Map<String, Set<String>> colToSelectedValues = new HashMap<>();

        colToSelectedValues.put(columnsChoiceBox.getValue().replace("Column ", "").trim(),
                new HashSet<>(valuesListView.getSelectionModel().getSelectedItems()));

        for (int i = 0; i < addedColumns.size(); i++) {
            colToSelectedValues.put(addedColumns.get(i).getValue().replace("Column ", "").trim(),
                    new HashSet<>(addedValues.get(i).getSelectionModel().getSelectedItems()));
        }

        getRangePopUpController.filter(colToSelectedValues);

        if(currentPopupStage != null) {
            currentPopupStage.close();
        }
    }
}
