package components.graph;

import impl.cell.Cell;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.AppController;

import java.util.ArrayList;
import java.util.List;

public class GetGraphRangePopUpController {

    @FXML
    private VBox getGraphRangePopUp;

    @FXML
    private ChoiceBox<String> xColumnsChoiceBox;

    @FXML
    private ChoiceBox<String> yColumnsChoiceBox;

    @FXML
    private TextField XAxisRangeTextField;

    @FXML
    private TextField YAxisRangeTextField;

    private GraphComponentController graphComponentController;

    private Stage currentPopupStage;


    public void setGraphComponentController(GraphComponentController graphComponentController) {
        this.graphComponentController = graphComponentController;
    }

    public void setCurrentPopupStage(Stage currentPopupStage) {
        this.currentPopupStage = currentPopupStage;
    }

    public void setColumnsChoiceBoxes() {
        int numOfColumns = graphComponentController.getNumOfColumns();
        List<String> columnChoices = generateColumnLabels(numOfColumns);
        xColumnsChoiceBox.getItems().addAll(columnChoices);
        yColumnsChoiceBox.getItems().addAll(columnChoices);

    }

    private List<String> generateColumnLabels(int numOfColumns) {
        List<String> columnLabels = new ArrayList<>();

        for (int i = 1; i <= numOfColumns; i++) {
            columnLabels.add(String.valueOf(Cell.getCellIDFromRowCol(1, i).charAt(0)));
        }

        return columnLabels;
    }

    @FXML
    void buildGraphOnClick() {
        try {
            String xColumn = xColumnsChoiceBox.getValue();
            String yColumn = yColumnsChoiceBox.getValue();
            String xRange = XAxisRangeTextField.getText();
            String yRange = YAxisRangeTextField.getText();

            if (xColumn == null || yColumn == null || xRange.isEmpty() || yRange.isEmpty()) {
                AppController.showErrorDialog("Error", "Please make sure to select columns and enter ranges.");
                return;
            }

            int xStart = Integer.parseInt(xRange.split("-")[0]);
            int xEnd = Integer.parseInt(xRange.split("-")[1]);
            int yStart = Integer.parseInt(yRange.split("-")[0]);
            int yEnd = Integer.parseInt(yRange.split("-")[1]);

            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

            xAxis.setLabel("X Axis: Column " + xColumn);
            yAxis.setLabel("Y Axis: Column " + yColumn);

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Data from " + xColumn + " and " + yColumn);
            lineChart.setLegendVisible(false);

            int rowX = xStart;
            int rowY = yStart;

            while(rowX <= xEnd && rowY <= yEnd) {
                try{
                    double xValue = graphComponentController.getValueFromCell(rowX, xColumn);
                    double yValue = graphComponentController.getValueFromCell(rowY, yColumn);
                    series.getData().add(new XYChart.Data<>(xValue, yValue));
                    rowX++;
                    rowY++;
                }
                catch (NumberFormatException e) {
                AppController.showErrorDialog("Error", "Make sure the cells contains only numeric values.");
                return;
                }
            }

            lineChart.getData().add(series);

            showGraphPopUp(lineChart);

        } catch (NumberFormatException e) {
            AppController.showErrorDialog("Error", "Invalid range format. Please enter the range in the format 'First row-Last row' (example: 1-3).");
        }
    }

    private void showGraphPopUp(LineChart<Number, Number> lineChart) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.DECORATED);

        VBox popupLayout = new VBox();
        popupLayout.getChildren().add(lineChart);

        Scene popupScene = new Scene(popupLayout, 800, 400);
        popupStage.setScene(popupScene);
        popupStage.setTitle("Graph View");

        if (currentPopupStage != null) {
            currentPopupStage.close();
        }

        popupStage.show();
    }


}
