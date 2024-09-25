package components.maingrid;

import components.bonuses.BonusesController;
import components.maingrid.cell.CellComponentController;
import dto.CellDTO;
import dto.SheetDTO;
import impl.cell.Cell;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import main.AppController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGridController {

    @FXML
    private GridPane mainGrid;

    private AppController appController;

    private final Map<String, CellComponentController> cellComponentControllers = new HashMap<>();

    private final Map<String, CellComponentController> cellComponentControllersBorder = new HashMap<>();

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void buildGridBoundaries(SheetDTO sheetDTO) throws IOException {

        int numOfRows = sheetDTO.getNumOfRows();
        int numOfCols = sheetDTO.getNumOfCols();

        CellComponentController borderCell = createCell("",0,0, true);
        cellComponentControllersBorder.put("0", borderCell);

        for (int i = 1; i <= numOfRows; i++) {
            String row = String.format("%02d", i);
            CellComponentController cell = createCell(row, i,0, true);
            cellComponentControllersBorder.put(row, cell);
        }

        for (int i = 1; i <= numOfCols; i++) {
            String col = String.format("%c", i - 1 + 'A');
            CellComponentController cell = createCell(col, 0, i, true);
            cellComponentControllersBorder.put(col, cell);
        }
    }

    public void disableGrid(boolean disable){
        mainGrid.setMouseTransparent(disable);
    }

    public void createInnerCellsInGrid(SheetDTO sheetDTO) throws IOException {
        for(CellDTO cellDTO : sheetDTO.getActiveCells().values())
        {
            CellComponentController cell = cellComponentControllers.get(cellDTO.getIdentity());
            Object effectiveValue = cellDTO.getEffectiveValue().getValue();
            String effectiveValueStr = effectiveValue.toString();

            if(effectiveValue instanceof Boolean){
                effectiveValueStr = effectiveValueStr.toUpperCase();
            }

            cell.setEffectiveValue(effectiveValueStr);
            cell.setCell(cellDTO);
            applyFadeInAnimation(cell);
        }

    }

    private void applyFadeInAnimation(CellComponentController cell) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(cell.getCellLabel());
        fadeTransition.setDuration(Duration.millis(1000));
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        if(BonusesController.animationsEnabledProperty.get())
            fadeTransition.play();
    }

    private CellComponentController createCell(String effectiveValue, int row, int column, boolean isDisable) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainGridController.class.getResource("/components/maingrid/cell/CellComponent.fxml"));
        Region newCell = loader.load();
        newCell.setDisable(isDisable);
        CellComponentController cellComponentController = loader.getController();
        cellComponentController.setEffectiveValue(effectiveValue);
        cellComponentController.setAppController(appController);
        cellComponentController.setCellSize();
        GridPane.setColumnIndex(newCell, column + 1);
        GridPane.setRowIndex(newCell, row + 1);
        mainGrid.getChildren().add(newCell);
        newCell.getStyleClass().add("grid-cell");

        if (row == 0 || column == 0 || row == mainGrid.getRowCount() + 1 || column == mainGrid.getColumnCount() + 1) {
            newCell.getStyleClass().add("edge");
        }

        return cellComponentController;
    }

    public void createDynamicGrid(SheetDTO sheetDTO) throws IOException {
        mainGrid.getChildren().clear();
        mainGrid.getColumnConstraints().clear();
        mainGrid.getRowConstraints().clear();
        cellComponentControllers.clear();

        int numOfRows = sheetDTO.getNumOfRows();
        int numOfCols = sheetDTO.getNumOfCols();

        createColsInGrid(numOfCols);
        createRowsInGrid(numOfRows);
        createEmptyCellsInGrid(numOfRows, numOfCols);

    }

    private void createEmptyCellsInGrid(int numOfRows, int numOfCols) throws IOException {
        for (int row = 1; row <= numOfRows; row++) {
            for (int col = 1; col <= numOfCols; col++) {
                String identity = String.format("%c", col + 'A' - 1) + row;
                CellComponentController cellComponentController = createCell("", row, col, false);
                cellComponentController.setCell(new CellDTO(identity));
                cellComponentControllers.put(identity, cellComponentController);
            }
        }
    }

    private void createRowsInGrid(int numOfRows) {
        for (int row = 0; row <= numOfRows + 2 ; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(Region.USE_COMPUTED_SIZE);
            rowConstraints.setMaxHeight(Double.MAX_VALUE);
            rowConstraints.setMinHeight(Region.USE_COMPUTED_SIZE);


            if (row == 0 || row == numOfRows + 2) {
                rowConstraints.setVgrow(Priority.ALWAYS);
            } else {
                rowConstraints.setVgrow(Priority.NEVER);
            }

            mainGrid.getRowConstraints().add(rowConstraints);
        }

    }

    private void createColsInGrid(int numOfCols) {
        for (int col = 0; col <= numOfCols + 2 ; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(Region.USE_COMPUTED_SIZE);
            colConstraints.setMaxWidth(Double.MAX_VALUE);
            colConstraints.setMinWidth(Region.USE_COMPUTED_SIZE);


            if (col == 0 || col == numOfCols + 2) {

                colConstraints.setHgrow(Priority.ALWAYS);
            } else {

                colConstraints.setHgrow(Priority.NEVER);
            }
            mainGrid.getColumnConstraints().add(colConstraints);
        }

    }


    public void activateMouseClickedOfCell(String selectedCellId) {
        cellComponentControllers.get(selectedCellId).onMouseClicked();
    }

    public void updateRowConstraints(int rowIndex, int height) {

        RowConstraints rowConstraints = mainGrid.getRowConstraints().get(rowIndex);
        rowConstraints.setPrefHeight(height);

        for (Node node : mainGrid.getChildren()) {
            // קבלת אינדקס השורה של הרכיב הנוכחי
            Integer currentRowIndex = GridPane.getRowIndex(node);

            // בדיקה אם הרכיב נמצא בשורה הנכונה והאם הוא מסוג Label
            if (currentRowIndex != null && currentRowIndex == rowIndex && node instanceof Label) {
                // עדכון ה-prefHeight של ה-Label
                ((Label) node).setPrefHeight(height);
            }
        }

    }

    public void updateColConstraints(int colIndex, int width) {

        ColumnConstraints columnConstraints = mainGrid.getColumnConstraints().get(colIndex);
        columnConstraints.setPrefWidth(width);

        for (Node node : mainGrid.getChildren()) {
            // קבלת אינדקס העמודה של הרכיב הנוכחי
            Integer currentColIndex = GridPane.getColumnIndex(node);

            // בדיקה אם הרכיב נמצא בעמודה הנכונה והאם הוא מסוג Label
            if (currentColIndex != null && currentColIndex == colIndex && node instanceof Label) {
                // עדכון ה-prefWidth של ה-Label
                ((Label) node).setPrefWidth(width);
            }
        }

    }

    public void updateColAlignment(int columnIndex, String alignment) {
        for (Node node : mainGrid.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            if (col != null && col == columnIndex + 1 && row != null && row != 1) {
                if (alignment.equals("Left")) {
                    node.getStyleClass().removeAll("align-center", "align-right");
                    node.getStyleClass().add("align-left");
                } else if (alignment.equals("Center")) {
                    node.getStyleClass().removeAll("align-left", "align-right");
                    node.getStyleClass().add("align-center");
                } else if (alignment.equals("Right")) {
                    node.getStyleClass().removeAll("align-left", "align-center");
                    node.getStyleClass().add("align-right");
                }
            }
        }
    }

    public CellComponentController getCellController(String cellId) {
        return cellComponentControllers.get(cellId);
    }

    public void markCellsInRange(List<CellDTO> cells) {
        for (CellDTO cell : cells) {
            CellComponentController cellController = getCellController(cell.getIdentity());
            if (cellController != null) {
                if(!cellController.getCellLabel().getStyleClass().contains("marked-cell"))
                    cellController.getCellLabel().getStyleClass().add("marked-cell");
            }
        }
    }

    public void unmarkCellsInRange(List<CellDTO> cells) {
        for (CellDTO cell : cells) {
            CellComponentController cellController = getCellController(cell.getIdentity());
            if (cellController != null) {
                cellController.getCellLabel().getStyleClass().remove("marked-cell");
            }
        }
    }


}
