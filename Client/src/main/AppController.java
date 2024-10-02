package main;

import api.CellValue;
import api.DTO;
import api.Engine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.actionline.ActionLineController;
import components.bonuses.BonusesController;
import components.commands.CommandsComponentController;
import components.graph.GraphComponentController;
import components.login.LoginController;
import components.maingrid.MainGridController;
import components.maingrid.cell.CellComponentController;
import components.ranges.RangesController;
import components.sortandfilter.SortAndFilterController;
import components.versions.VersionsSelectorComponentController;
import dto.CellDTO;
import dto.DTOFactoryImpl;
import dto.RangeDTO;
import dto.SheetDTO;
import impl.EngineImpl;
import impl.cell.Cell;
import impl.cell.value.NumericValue;
import impl.sheet.SheetData;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import utils.CellValueAdapter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static impl.cell.Cell.getColumnFromCellID;
import static impl.cell.Cell.getRowFromCellID;
import static utils.Constants.*;


public class AppController {

    @FXML
    private ScrollPane rootPane;

    @FXML
    private HBox hBoxContainer;

    @FXML
    private GridPane mainGridComponent;

    @FXML
    private MainGridController mainGridComponentController;

    @FXML
    private HBox actionLineComponent;

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
    private Label usernameLabel;

    @FXML
    private Button backButton;

    @FXML
    private GraphComponentController graphComponentController;

    private Stage sheetPopUpStage;  // משתנה סינגלטון עבור ה-Stage

    private final IntegerProperty currentPreviousVersion = new SimpleIntegerProperty();  // נכס עבור מספר הגרסה

   private final Engine engine = new EngineImpl(new DTOFactoryImpl());

    private SheetData selectedSheet;

    @FXML
    public void initialize() {
        mainGridComponentController.setAppController(this);
        actionLineComponentController.setAppController(this);
        commandsComponentController.setAppController(this);
        rangesComponentController.setAppController(this);
        sortAndFilterComponentController.setAppController(this);
        graphComponentController.setAppController(this);
        versionsSelectorComponentController.setAppController(this);
        bonusesComponentController.setAppController(this);

        try {
            sheetPopUpStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void backButtonClicked() throws IOException {
        backButton.getScene().getWindow().hide();
        URL appPage = getClass().getResource(SHEET_MANAGER_FXML_RESOURCE_LOCATION);
        LoginController.openSheetManager(appPage, (Stage) backButton.getScene().getWindow(), usernameLabel.getText().replaceAll("Username: ", ""));
    }

    public void displayCellDataOnActionLine(CellDTO cell) {
        actionLineComponentController.displayCellData(cell);
    }


    public void updateCellDataToEngine(String selectedCellId, String newValue) throws IOException {
        // בניית URL עם query parameters באמצעות HttpUrl
        String finalUrl = HttpUrl
                .parse(UPDATE_CELL)
                .newBuilder()
                .addQueryParameter("cellId", selectedCellId)
                .addQueryParameter("newValue", newValue)
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = createConnection(url);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת SheetData ל-JSON
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        // המרת JsonObject למחרוזת JSON
        byte[] postDataBytes = sheetDataJson.getBytes(StandardCharsets.UTF_8);

        // שליחת הנתונים לשרת (SheetData כ-body)
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postDataBytes);
        }

        // בדיקת קוד התגובה של השרת
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to update cell, response code: " + responseCode);
        }

        Platform.runLater(() -> {
            try {
                refreshGridAfterCellUpdate(selectedCellId);
            } catch (IOException e) {
                showErrorDialog("Error", "Failed to refresh grid after cell update.");
            }
        });
    }

    public static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        return connection;
    }

    // Refresh grid after cell update
    private void refreshGridAfterCellUpdate(String selectedCellId) throws IOException {
        SheetDTO updatedSheet = getUpdatedSheetDTOFromServer();
        mainGridComponentController.createInnerCellsInGrid(updatedSheet);
        mainGridComponentController.activateMouseClickedOfCell(selectedCellId);
        versionsSelectorComponentController.updateVersionsSelector();
    }

    private SheetDTO getUpdatedSheetDTOFromServer() throws IOException {
        // בניית URL לשרת
        URL url = new URL(GET_SHEET_DTO);

        // פתיחת חיבור לשרת
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true); // מאפשר כתיבת תוכן בבקשת ה-POST
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


        // המרת אובייקט ה-SheetData ל-JSON ושליחתו
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CellValue.class, new CellValueAdapter())
                .create();
        String jsonInputString = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // קבלת תגובת השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // שימוש ב-Gson כדי להמיר את התגובה ל-SheetDTO
                return gson.fromJson(bufferedReader, SheetDTO.class);
            }
        } else {
            throw new IOException("Failed to get updated sheet from server. Response code: " + responseCode);
        }
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

    // פונקציה להצגת הודעת שגיאה
    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public double getPrefRowHeight() throws IOException {
        return getUpdatedSheetDTOFromServer().getRowHeight();
    }

    public double getPrefColWidth() throws IOException {
        return getUpdatedSheetDTOFromServer().getColWidth();
    }

    public void updateColumnAlignment(int columnIndex, String alignment) {
        mainGridComponentController.updateColAlignment(columnIndex, alignment);
    }

    public CellComponentController getCellControllerById(String cellId) {
        return mainGridComponentController.getCellController(cellId);
    }

    public boolean checkIfRowExist(int rowIndex) throws IOException {
        return rowIndex <= getUpdatedSheetDTOFromServer().getNumOfRows() && rowIndex >= 1;
    }

    public void setRowHeightInGrid(int rowIndex, int height) {
        mainGridComponentController.updateRowConstraints(rowIndex, height);
    }

    public boolean checkIfColExist(int colIndex) throws IOException {
        return colIndex <= getUpdatedSheetDTOFromServer().getNumOfCols() && colIndex >= 1;
    }

    public void setColWidthInGrid(int rowIndex, int width) {
        mainGridComponentController.updateColConstraints(rowIndex, width);
    }

    public void addNewRange(String topLeftCell, String bottomRightCell, String rangeName) throws IOException {

        String finalUrl = HttpUrl
                .parse(ADD_NEW_RANGE)
                .newBuilder()
                .addQueryParameter("topLeftCell", topLeftCell)
                .addQueryParameter("bottomRightCell", bottomRightCell)
                .addQueryParameter("rangeName", rangeName)
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        // המרת selectedSheet ל-JSON ושליחתו בגוף הבקשה
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // בדיקת תגובת השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Range added successfully.");
        } else {
            throw new IOException("Failed to add range, response code: " + responseCode);
        }

    }

    public void markCellsInRange(String rangeName) throws IOException {
        updateCellsInRange(rangeName, true);
    }

    public void unmarkCellsInRange(String rangeName) throws IOException {
        updateCellsInRange(rangeName, false);
    }

    public void updateCellsInRange(String rangeName, boolean mark) throws IOException {
        // בניית URL עם הפרמטר של rangeName
        String finalUrl = HttpUrl
                .parse(GET_RANGE_DTO)
                .newBuilder()
                .addQueryParameter("rangeName", rangeName)
                .build()
                .toString();

        // יצירת חיבור ושליחת SheetData
        HttpURLConnection connection = createConnectionWithSheetData(finalUrl);

        // קבלת RangeDTO מהשרת
        RangeDTO rangeDTO = getRangeDTOFromServer(connection);

        // סימון או הסרת סימון התאים בטווח על בסיס הערך של 'mark'
        if (mark) {
            mainGridComponentController.markCellsInRange(rangeDTO.getCells());
        } else {
            mainGridComponentController.unmarkCellsInRange(rangeDTO.getCells());
        }
    }

    private HttpURLConnection createConnectionWithSheetData(String finalUrl) throws IOException {
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        // המרת selectedSheet ל-JSON ושליחתו בגוף הבקשה
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return connection;
    }

    private RangeDTO getRangeDTOFromServer(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // המרת התגובה ל-RangeDTO
                Gson gson = GSON_INSTANCE;
                return gson.fromJson(bufferedReader, RangeDTO.class);
            }
        } else {
            throw new IOException("Failed to update cells in range, response code: " + responseCode);
        }
    }


    public Map<Integer, DTO> getSheetsPreviousVersionsDTO() {
        return engine.getSheetsPreviousVersionsDTO(selectedSheet);
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
            SheetDTO previousSheetDTO = (SheetDTO) engine.getSheetsPreviousVersionsDTO(selectedSheet).get(selectedVersion);
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
        RangeDTO rangeDTO = (RangeDTO) engine.getRangeDTOFromSheet(rangeName, selectedSheet);
        engine.deleteRangeFromSheet(rangeName, selectedSheet);
        mainGridComponentController.unmarkCellsInRange(rangeDTO.getCells());
    }

    public boolean checkRangeOfCells(String topLeft, String bottomRight) {
        return engine.isCellInBounds(Cell.getRowFromCellID(topLeft) - 1, Cell.getColumnFromCellID(topLeft) - 1, selectedSheet)
                && engine.isCellInBounds(Cell.getRowFromCellID(bottomRight) - 1, Cell.getColumnFromCellID(bottomRight) - 1, selectedSheet);
    }

    public void sortSheetByColumns(List<String> columnToSortBy, String topLeft, String bottomRight) {
        try {
            sheetPopUpStage.titleProperty().unbind();
            sheetPopUpStage.setTitle("Sorted Sheet");
            SheetDTO sortedSheetDTO = (SheetDTO) engine.getSortedSheetDTO(columnToSortBy, topLeft, bottomRight, selectedSheet);
            displaySheetPopUp(sortedSheetDTO, topLeft, bottomRight);
        }
        catch (IOException ignored) {
        }
        catch (NumberFormatException e){
            showErrorDialog("Error", e.getMessage());
        }

    }

    public Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight) {

        return engine.getValuesFromColumn(column, topLeft, bottomRight, selectedSheet);
    }

    public void filter(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight) {
        try {
            sheetPopUpStage.titleProperty().unbind();
            sheetPopUpStage.setTitle("Filtered Sheet");
            SheetDTO filteredSheetDTO = (SheetDTO) engine.getFilteredSheetDTO(colToSelectedValues, topLeft, bottomRight, selectedSheet);
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
                stylesheets.add(getClass().getResource("/main/resources/styles/darkTheme.css").toExternalForm());
                rootPane.getStyleClass().add("dark-mode"); // Add dark-mode class
                break;
            case "light":
                stylesheets.add(getClass().getResource("/main/resources/styles/lightTheme.css").toExternalForm());
                rootPane.getStyleClass().add("light-mode"); // Add light-mode class
                break;
            default:
                stylesheets.add(getClass().getResource("/main/resources/styles/default.css").toExternalForm());
                break;
        }
    }

    public double getCellValue(int row, String col) {
        CellDTO cell = getCellDTOFromServer(Cell.getCellIDFromRowCol(row,Cell.getColumnFromCellID(col)));
        return Double.parseDouble(cell.getEffectiveValue().getValue().toString());
    }

    public int getNumOfColumnsInGrid() {
        return engine.getNumOfColumnsInCurrSheet(selectedSheet);
    }

    public void showDynamicCalculation(String selectedCellId, String orgValue) throws IOException {

        CellValue newCellValue = EngineImpl.convertStringToCellValue(orgValue);
        mainGridComponentController.createInnerCellsInGrid((SheetDTO) engine.DynamicCalculationOnSheet(selectedCellId, newCellValue, orgValue, selectedSheet));
    }

    public void showCurrentSheetOnGrid() throws IOException {
        mainGridComponentController.createInnerCellsInGrid((SheetDTO) engine.getSheetDTO(selectedSheet));
    }

    public boolean isCellValueNumeric(String cellId) {
        return getCellDTOFromServer(cellId).getEffectiveValue() instanceof NumericValue;
    }

    private CellDTO getCellDTOFromServer(String cellId) {
        try {
            String finalUrl = HttpUrl
                    .parse(GET_CELL_DTO)
                    .newBuilder()
                    .addQueryParameter("cellId", cellId)
                    .build()
                    .toString();

            URL url = new URL(finalUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // המרת SheetData ל-JSON
            Gson gson = GSON_INSTANCE;
            String sheetDataJson = gson.toJson(selectedSheet);

            // המרת JsonObject למחרוזת JSON
            byte[] postDataBytes = sheetDataJson.getBytes(StandardCharsets.UTF_8);

            // שליחת הנתונים לשרת (SheetData כ-body)
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postDataBytes);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream();
                     InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    return gson.fromJson(bufferedReader, CellDTO.class);
                }
            } else {
                throw new IOException("Failed to get cell DTO from server. Response code: " + responseCode);
            }
        } catch (IOException e) {
            showErrorDialog("Error", "Failed to get cell DTO from server.");
            return null;
        }
    }

    public void setNewSelectedSheet(SheetData selectedSheet) throws IOException {
        this.selectedSheet = selectedSheet;
        SheetDTO sheetDTO = getUpdatedSheetDTOFromServer();
        mainGridComponentController.createDynamicGrid(sheetDTO);
        mainGridComponentController.buildGridBoundaries(sheetDTO);
        mainGridComponentController.createInnerCellsInGrid(sheetDTO);
        versionsSelectorComponentController.updateVersionsSelector();
    }

    public void setUsername(String username) {
        usernameLabel.setText(usernameLabel.getText() + username);
    }
}
