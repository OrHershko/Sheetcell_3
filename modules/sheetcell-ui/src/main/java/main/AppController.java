package main;


import api.CellValue;
import api.Engine;
import components.actionline.ActionLineController;
import components.bonuses.BonusesController;
import components.commands.CommandsComponentController;
import components.graph.GraphComponentController;
import components.loadfile.LoadFileController;
import components.maingrid.MainGridController;
import components.maingrid.cell.CellComponentController;
import components.ranges.RangesController;
import components.sortandfilter.SortAndFilterController;
import components.versions.VersionsSelectorComponentController;
import dto.CellDTO;
import dto.DTO;
import dto.RangeDTO;
import dto.SheetDTO;
import impl.DTOFactoryImpl;
import impl.EngineImpl;
import impl.cell.Cell;
import static impl.cell.Cell.getColumnFromCellID;
import static impl.cell.Cell.getRowFromCellID;
import impl.sheet.SheetData;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class AppController {

    @FXML
    private ScrollPane rootPane;

    @FXML
    private HBox hBoxContainer;

    @FXML
    private GridPane loadFileComponent;

    @FXML
    private LoadFileController loadFileComponentController;

    @FXML
    private GridPane mainGridComponent;

    @FXML
    private MainGridController mainGridComponentController;

    @FXML
    private GridPane actionLineComponent;

    @FXML
    private ActionLineController actionLineComponentController;

    @FXML
    private VBox commandsComponent;

    @FXML
    private CommandsComponentController commandsComponentController;

    @FXML
    private VBox rangesComponent;

    @FXML
    private RangesController rangesComponentController;

    @FXML
    private MenuButton versionsSelectorComponent;

    @FXML
    private VBox sortAndFilterComponent;

    @FXML
    private SortAndFilterController sortAndFilterComponentController;

    @FXML
    private VersionsSelectorComponentController versionsSelectorComponentController;

    @FXML
    private HBox bonusesComponent;

    @FXML
    private BonusesController bonusesComponentController;

    @FXML
    private Button graphComponent;

    @FXML
    private GraphComponentController graphComponentController;

    private final Engine engine = new EngineImpl(new DTOFactoryImpl());
    private SheetData currentSheetData; // Track current sheet data

    private Stage sheetPopUpStage;  // Singleton instance for the popup Stage
    private final IntegerProperty currentPreviousVersion = new SimpleIntegerProperty();  // Property for the previous version number being viewed

    @FXML
    public void initialize() {
        loadFileComponentController.setAppController(this);
        mainGridComponentController.setAppController(this);
        actionLineComponentController.setAppController(this);
        commandsComponentController.setAppController(this);
        rangesComponentController.setAppController(this);
        versionsSelectorComponentController.setAppController(this);
        sortAndFilterComponentController.setAppController(this);
        bonusesComponentController.setAppController(this);
        graphComponentController.setAppController(this);
        try {
            sheetPopUpStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFileToEngine(File selectedFile) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
            engine.loadFile(fileInputStream, "DefaultUser");
            // Get the sheet data after loading - assume single sheet for now
            currentSheetData = engine.getSheetData(selectedFile.getName().replace(".xml", ""));
            if (currentSheetData == null) {
                // Fallback: get the first available sheet
                List<SheetData> sheets = engine.getAllSheetsData("DefaultUser");
                if (!sheets.isEmpty()) {
                    currentSheetData = sheets.get(0);
                }
            }
        }
        Platform.runLater(() -> {
            try {
                if (currentSheetData != null) {
                    mainGridComponentController.createDynamicGrid((SheetDTO) engine.getSheetDTO(currentSheetData));
                    mainGridComponentController.buildGridBoundaries((SheetDTO) engine.getSheetDTO(currentSheetData));
                    mainGridComponentController.createInnerCellsInGrid((SheetDTO) engine.getSheetDTO(currentSheetData));
                }
                commandsComponentController.disableButtons(false);
                rangesComponentController.disableButtons(false);
                versionsSelectorComponentController.disable(false);
                sortAndFilterComponentController.disableButtons(false);
                graphComponentController.setDisable(false);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void displayCellDataOnActionLine(CellDTO cell) {
        actionLineComponentController.displayCellData(cell);
    }

    public void updateCellDataToEngine(String selectedCellId, String orgValue) throws IOException {
        if (currentSheetData == null) {
            throw new IllegalStateException("No sheet loaded");
        }

        CellValue newCellValue = EngineImpl.convertStringToCellValue(orgValue);
        engine.updateCellValue(selectedCellId, newCellValue, orgValue, currentSheetData, "DefaultUser");
        mainGridComponentController.createInnerCellsInGrid((SheetDTO) engine.getSheetDTO(currentSheetData));
        mainGridComponentController.activateMouseClickedOfCell(selectedCellId);
        versionsSelectorComponentController.updateVersionsSelector();
    }

    public void colorDependencies(Set<String> cells, String styleClass) {

        for (Node node : mainGridComponent.getChildren()) {
            node.getStyleClass().remove(styleClass);
        }


        for (String cellID : cells) {
            int column = getColumnFromCellID(cellID) + 1;
            int row = getRowFromCellID(cellID) + 1;

            for (Node node : mainGridComponent.getChildren()) {
                if (GridPane.getColumnIndex(node) == column && GridPane.getRowIndex(node) == row) {

                    node.getStyleClass().add(styleClass);
                    break;
                }
            }

        }
    }

    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public double getPrefRowHeight() {
        if (currentSheetData == null) return 25.0; // default height
        return ((SheetDTO) engine.getSheetDTO(currentSheetData)).getRowHeight();
    }

    public double getPrefColWidth() {
        if (currentSheetData == null) return 75.0; // default width
        return ((SheetDTO) engine.getSheetDTO(currentSheetData)).getColWidth();
    }

    public void updateColumnAlignment(int columnIndex, String alignment) {
        mainGridComponentController.updateColAlignment(columnIndex, alignment);
    }

    public CellComponentController getCellControllerById(String cellId) {
        return mainGridComponentController.getCellController(cellId);
    }

    public boolean checkIfRowExist(int rowIndex) {
        if (currentSheetData == null) return false;
        return rowIndex <= ((SheetDTO) engine.getSheetDTO(currentSheetData)).getNumOfRows() && rowIndex >= 1;
    }

    public void setRowHeightInGrid(int rowIndex, int height) {
        mainGridComponentController.updateRowConstraints(rowIndex, height);
    }

    public boolean checkIfColExist(int colIndex) {
        if (currentSheetData == null) return false;
        return colIndex <= ((SheetDTO) engine.getSheetDTO(currentSheetData)).getNumOfCols() && colIndex >= 1;
    }

    public void setColWidthInGrid(int rowIndex, int width) {
        mainGridComponentController.updateColConstraints(rowIndex, width);
    }

    public void addNewRange(String topLeftCell, String bottomRightCell, String rangeName) {
        if (currentSheetData == null) {
            throw new IllegalStateException("No sheet loaded");
        }
        engine.addNewRange(topLeftCell, bottomRightCell, rangeName, currentSheetData);
    }

    public void markCellsInRange(String rangeName) {
        if (currentSheetData == null) return;
        RangeDTO rangeDTO = (RangeDTO) engine.getRangeDTOFromSheet(rangeName, currentSheetData);
        mainGridComponentController.markCellsInRange(rangeDTO.getCells());
    }

    public void unmarkCellsInRange(String rangeName) {
        if (currentSheetData == null) return;
        RangeDTO rangeDTO = (RangeDTO) engine.getRangeDTOFromSheet(rangeName, currentSheetData);
        mainGridComponentController.unmarkCellsInRange(rangeDTO.getCells());
    }

    public Map<Integer, DTO> getSheetsPreviousVersionsDTO() {
        if (currentSheetData == null) {
            throw new IllegalStateException("No sheet loaded");
        }
        return engine.getSheetsPreviousVersionsDTO(currentSheetData);
    }

    public void loadPreviousVersion(int selectedVersion) {
        try {
            currentPreviousVersion.set(selectedVersion);
            sheetPopUpStage.titleProperty().bind(
                    currentPreviousVersion.asString("Previous Sheet Version - Version %d")
            );
            ScrollPane scrollPane = (ScrollPane) sheetPopUpStage.getScene().getRoot();
            GridPane gridPane = (GridPane) scrollPane.getContent();
            MainGridController controller = (MainGridController) gridPane.getUserData();
            SheetDTO previousSheetDTO = (SheetDTO) engine.getSheetsPreviousVersionsDTO(currentSheetData).get(selectedVersion);
            controller.createDynamicGrid(previousSheetDTO);
            controller.buildGridBoundaries(previousSheetDTO);
            controller.createInnerCellsInGrid(previousSheetDTO);
            controller.disableGrid(true);

            if (!sheetPopUpStage.isShowing()) {
                sheetPopUpStage.show();
            }

        } catch (IOException e) {
            showErrorDialog("Error", "Failed to load previous version.");
        }
    }

    private void sheetPopUpStage() throws IOException {
        if (sheetPopUpStage == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/maingrid/MainGrid.fxml"));
            Parent root = loader.load();
            MainGridController controller = loader.getController();
            controller.setAppController(this);

            ScrollPane scrollPane = new ScrollPane(root);
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);

            sheetPopUpStage = new Stage();

            sheetPopUpStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(scrollPane);
            scene.getStylesheets().add(getClass().getResource("/components/maingrid/cell/CellComponent.css").toExternalForm());
            sheetPopUpStage.setScene(scene);
            root.setUserData(controller);
        }
    }

    public void deleteExistingRange(String rangeName) {
        if (currentSheetData == null) return;
        RangeDTO rangeDTO = (RangeDTO) engine.getRangeDTOFromSheet(rangeName, currentSheetData);
        engine.deleteRangeFromSheet(rangeName, currentSheetData);
        mainGridComponentController.unmarkCellsInRange(rangeDTO.getCells());
    }

    public boolean checkRangeOfCells(String topLeft, String bottomRight) {
        if (currentSheetData == null) return false;
        return engine.isCellInBounds(Cell.getRowFromCellID(topLeft) - 1, Cell.getColumnFromCellID(topLeft) - 1, currentSheetData)
                && engine.isCellInBounds(Cell.getRowFromCellID(bottomRight) - 1, Cell.getColumnFromCellID(bottomRight) - 1, currentSheetData);
    }

    public void sortSheetByColumns(List<String> columnToSortBy, String topLeft, String bottomRight) {
        try {
            if (currentSheetData == null) return;
            sheetPopUpStage.titleProperty().unbind();
            sheetPopUpStage.setTitle("Sorted Sheet");
            SheetDTO sortedSheetDTO = (SheetDTO) engine.getSortedSheetDTO(columnToSortBy, topLeft, bottomRight, currentSheetData);
            displaySheetPopUp(sortedSheetDTO, topLeft, bottomRight);
        }
        catch (IOException ignored) {
        }
        catch (NumberFormatException e){
            showErrorDialog("Error", e.getMessage());
        }

    }

    public Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight) {
        if (currentSheetData == null) return new HashSet<>();
        return engine.getValuesFromColumn(column, topLeft, bottomRight, currentSheetData);
    }

    public void filter(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight) {
        try {
            if (currentSheetData == null) return;
            sheetPopUpStage.titleProperty().unbind();
            sheetPopUpStage.setTitle("Filtered Sheet");
            SheetDTO filteredSheetDTO = (SheetDTO) engine.getFilteredSheetDTO(colToSelectedValues, topLeft, bottomRight, currentSheetData);
            displaySheetPopUp(filteredSheetDTO, topLeft, bottomRight);
        } catch (IOException ignored) {
        }

    }

    private void displaySheetPopUp(SheetDTO filteredSheetDTO, String topLeft, String bottomRight) throws IOException {
        ScrollPane scrollPane = (ScrollPane) sheetPopUpStage.getScene().getRoot();
        GridPane gridPane = (GridPane) scrollPane.getContent();
        MainGridController controller = (MainGridController) gridPane.getUserData();
        controller.createDynamicGrid(filteredSheetDTO);
        controller.buildGridBoundaries(filteredSheetDTO);
        controller.createInnerCellsInGrid(filteredSheetDTO);
        //controller.markFilterArea(topLeft, bottomRight);
        controller.disableGrid(true);

        if (!sheetPopUpStage.isShowing()) {
            sheetPopUpStage.show();
        }
    }

    public void applySkin(String skinName) {
        Scene scene = rootPane.getScene();
        ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.clear();
        scene.getStylesheets().add(getClass().getResource("/components/maingrid/cell/CellComponent.css").toExternalForm());

        // Clear any existing skin-specific classes
        rootPane.getStyleClass().removeAll("dark-mode", "light-mode");

        switch (skinName) {
            case "dark":
                stylesheets.add(getClass().getResource("/styles/darkTheme.css").toExternalForm());
                rootPane.getStyleClass().add("dark-mode"); // Add dark-mode class
                break;
            case "light":
                stylesheets.add(getClass().getResource("/styles/lightTheme.css").toExternalForm());
                rootPane.getStyleClass().add("light-mode"); // Add light-mode class
                break;
            default:
                stylesheets.add(getClass().getResource("/styles/default.css").toExternalForm());
                break;
        }
    }

    public double getCellValue(int row, String col) {
        if (currentSheetData == null) return 0.0;
        CellDTO cell = (CellDTO) engine.getCellDTO(Cell.getCellIDFromRowCol(row,Cell.getColumnFromCellID(col)), currentSheetData);
        return Double.parseDouble(cell.getEffectiveValue()); // effectiveValue is already a String
    }

    public int getNumOfColumnsInGrid() {
        if (currentSheetData == null) return 0;
        return engine.getNumOfColumnsInCurrSheet(currentSheetData);
    }

    public void showDynamicCalculation(String selectedCellId, String orgValue) throws IOException {
        if (currentSheetData == null) return;

        CellValue newCellValue = EngineImpl.convertStringToCellValue(orgValue);
        mainGridComponentController.createInnerCellsInGrid((SheetDTO) engine.DynamicCalculationOnSheet(selectedCellId, newCellValue, orgValue, currentSheetData));
    }

    public void showCurrentSheetOnGrid() throws IOException {
        if (currentSheetData == null) return;
        mainGridComponentController.createInnerCellsInGrid((SheetDTO) engine.getSheetDTO(currentSheetData));
    }

    public boolean isCellValueNumeric(String cellId) {
        if (currentSheetData == null) return false;
        CellDTO cellDTO = (CellDTO) engine.getCellDTO(cellId, currentSheetData);
        String effectiveValue = cellDTO.getEffectiveValue();
        try {
            Double.parseDouble(effectiveValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
