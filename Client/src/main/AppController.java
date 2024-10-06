package main;

import api.CellValue;
import api.DTO;
import api.Engine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import utils.CellValueAdapter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    //private final Engine engine = new EngineImpl(new DTOFactoryImpl());

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
            buildSheetPopUpStage();
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

        HttpURLConnection connection = buildConnectionToGetRangeData(rangeName);
        RangeDTO rangeDTO = getRangeDTOFromServer(connection);

        if (mark) {
            mainGridComponentController.markCellsInRange(rangeDTO.getCells());
        } else {
            mainGridComponentController.unmarkCellsInRange(rangeDTO.getCells());
        }
    }

    private HttpURLConnection buildConnectionToGetRangeData(String rangeName) throws IOException {
        String finalUrl = HttpUrl
                .parse(GET_RANGE_DTO)
                .newBuilder()
                .addQueryParameter("rangeName", rangeName)
                .build()
                .toString();

        return createConnectionWithSheetData(finalUrl);
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


    public Map<Integer, DTO> getSheetsPreviousVersionsDTO() throws IOException {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(GET_SHEET_VERSIONS_ENDPOINT)) // תחליף ב-URL המתאים לשרת
                .url()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST"); // אתה יכול לשנות ל-GET אם אין צורך לשלוח תוכן בגוף הבקשה
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת SheetData ל-JSON ושליחתו בגוף הבקשה (אם צריך לשלוח מידע על הגיליון הנבחר)
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet); // selectedSheet זה האובייקט הנבחר
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // בדיקת קוד התגובה של השרת
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get previous sheet versions, response code: " + responseCode);
        }

        // קריאת התגובה מהשרת
        try (InputStream inputStream = connection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            // המרת התגובה ל-Map של גרסאות
            Type type = new TypeToken<Map<Integer, SheetDTO>>() {
            }.getType();
            return gson.fromJson(bufferedReader, type);

        }
    }




    public void loadPreviousVersion(int selectedVersion){
        try {
            currentPreviousVersion.set(selectedVersion);
            sheetPopUpStage.titleProperty().bind(
                    currentPreviousVersion.asString("Previous Sheet Version - Version %d")
            );
            ScrollPane scrollPane = (ScrollPane) sheetPopUpStage.getScene().getRoot();
            GridPane gridPane = (GridPane) scrollPane.getContent();
            MainGridController controller = (MainGridController) gridPane.getUserData();
            //SheetDTO previousSheetDTO = (SheetDTO) engine.getSheetsPreviousVersionsDTO(selectedSheet).get(selectedVersion);
            SheetDTO previousSheetDTO = (SheetDTO)getSheetsPreviousVersionsDTO().get(selectedVersion);
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


    private void buildSheetPopUpStage() throws IOException {
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
            sheetPopUpStage.sizeToScene();
        }
    }

    public void deleteExistingRange(String rangeName) throws IOException {
        //RangeDTO rangeDTO = (RangeDTO) engine.getRangeDTOFromSheet(rangeName, selectedSheet);

        HttpURLConnection getConnection = buildConnectionToGetRangeData(rangeName);
        RangeDTO rangeDTO = getRangeDTOFromServer(getConnection);

        //engine.deleteRangeFromSheet(rangeName, selectedSheet);

        String finalUrl = HttpUrl
                .parse(DELETE_RANGE_FROM_SHEET)
                .newBuilder()
                .addQueryParameter("rangeName", rangeName)
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection deleteConnection = (HttpURLConnection) url.openConnection();
        deleteConnection.setRequestMethod("POST");
        deleteConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        deleteConnection.setDoOutput(true);

        // המרת selectedSheet ל-JSON
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        // שליחת SheetData כ-body
        try (OutputStream os = deleteConnection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // בדיקת קוד התגובה מהשרת
        int responseCode = deleteConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(deleteConnection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
                showErrorDialog("Error", errorResponse.toString());
            }
        }

        mainGridComponentController.unmarkCellsInRange(rangeDTO.getCells());
    }

//    public boolean checkRangeOfCells(String topLeft, String bottomRight) {
//        return engine.isCellInBounds(getRowFromCellID(topLeft) - 1, getColumnFromCellID(topLeft) - 1, selectedSheet)
//                && engine.isCellInBounds(getRowFromCellID(bottomRight) - 1, getColumnFromCellID(bottomRight) - 1, selectedSheet);
//    }
    public boolean checkRangeOfCells(String topLeft, String bottomRight) throws IOException {
        // בניית URL עם query parameters עבור topLeft ו-bottomRight
        String finalUrl = HttpUrl
                .parse(CHECK_RANGE_OF_CELLS)
                .newBuilder()
                .addQueryParameter("topLeft", topLeft)
                .addQueryParameter("bottomRight", bottomRight)
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת selectedSheet ל-JSON ושליחתו בגוף הבקשה
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // בדיקת קוד התגובה של השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // קריאת התשובה (אם התאים בטווח)
                return gson.fromJson(bufferedReader, Boolean.class);
            }
        } else {
            throw new IOException("Failed to check range of cells, response code: " + responseCode);
        }
    }


//    public void sortSheetByColumns(List<String> columnToSortBy, String topLeft, String bottomRight) {
//        try {
//            sheetPopUpStage.titleProperty().unbind();
//            sheetPopUpStage.setTitle("Sorted Sheet");
//            SheetDTO sortedSheetDTO = (SheetDTO) engine.getSortedSheetDTO(columnToSortBy, topLeft, bottomRight, selectedSheet);
//            displaySheetPopUp(sortedSheetDTO, topLeft, bottomRight);
//        }
//        catch (IOException ignored) {
//        }
//        catch (NumberFormatException e){
//            showErrorDialog("Error", e.getMessage());
//        }
//
//    }

    public void sortSheetByColumns(List<String> columnToSortBy, String topLeft, String bottomRight) throws IOException {
        // בניית URL עם query parameters עבור columnToSortBy, topLeft, ו-bottomRight
        String finalUrl = HttpUrl
                .parse(SORT_SHEET_BY_COLUMNS)
                .newBuilder()
                .addQueryParameter("topLeft", topLeft)
                .addQueryParameter("bottomRight", bottomRight)
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת הנתונים ל-JSON (רשימת העמודות וה-SheetData)
        Gson gson = GSON_INSTANCE;
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("columnToSortBy", gson.toJsonTree(columnToSortBy));
        jsonObject.add("sheetData", gson.toJsonTree(selectedSheet));

        String jsonInputString = gson.toJson(jsonObject);
        System.out.println("JSON sent to server: " + jsonInputString);

        byte[] postDataBytes = jsonInputString.getBytes(StandardCharsets.UTF_8);

        // שליחת הבקשה לשרת
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postDataBytes);
        }

        // בדיקת קוד התגובה של השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // המרת התגובה ל-SheetDTO
                SheetDTO sortedSheetDTO = gson.fromJson(bufferedReader, SheetDTO.class);

                // הצגת הגיליון הממויין בחלון פופ-אפ
                displaySheetPopUp(sortedSheetDTO, topLeft, bottomRight);
            }
        } else {
            throw new IOException("Failed to sort sheet, response code: " + responseCode);
        }
    }

//    public Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight) {
//
//        return engine.getValuesFromColumn(column, topLeft, bottomRight, selectedSheet);
//    }

    public Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight) throws IOException {
        // בניית URL עם query parameters עבור column, topLeft, ו-bottomRight
        String finalUrl = HttpUrl
                .parse(GET_VALUES_FROM_COLUMN)
                .newBuilder()
                .addQueryParameter("column", column)
                .addQueryParameter("topLeft", topLeft)
                .addQueryParameter("bottomRight", bottomRight)
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת selectedSheet ל-JSON ושליחתו בגוף הבקשה
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // קבלת תגובת השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // המרת התגובה ל-Set<String>
                Type setType = new TypeToken<Set<String>>(){}.getType();
                return gson.fromJson(bufferedReader, setType);
            }
        } else {
            throw new IOException("Failed to retrieve values from column, response code: " + responseCode);
        }
    }


//    public void filter(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight) {
//        try {
//            sheetPopUpStage.titleProperty().unbind();
//            sheetPopUpStage.setTitle("Filtered Sheet");
//            SheetDTO filteredSheetDTO = (SheetDTO) engine.getFilteredSheetDTO(colToSelectedValues, topLeft, bottomRight, selectedSheet);
//            displaySheetPopUp(filteredSheetDTO, topLeft, bottomRight);
//        } catch (IOException ignored) {
//        }
//
//    }
    public void filter(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight) throws IOException {
        // בניית URL עם query parameters עבור topLeft ו-bottomRight
        String finalUrl = HttpUrl
                .parse(FILTER_SHEET_URL) // קבוע עם ה-URL לפילטר של הגיליון
                .newBuilder()
                .addQueryParameter("topLeft", topLeft)
                .addQueryParameter("bottomRight", bottomRight)
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת map colToSelectedValues ל-JSON
        Gson gson = GSON_INSTANCE;
        JsonObject requestBody = new JsonObject();
        requestBody.add("colToSelectedValues", gson.toJsonTree(colToSelectedValues));  // המרת המפה ל-JSON
        requestBody.add("sheetData", gson.toJsonTree(selectedSheet)); // המרת selectedSheet ל-JSON

        // המרת JsonObject למחרוזת JSON ושליחתו לשרת
        byte[] postDataBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postDataBytes);
        }

        // קבלת תגובת השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // המרת התגובה ל-SheetDTO
                SheetDTO filteredSheetDTO = gson.fromJson(bufferedReader, SheetDTO.class);
                // הצגת הגיליון המפולטר
                displaySheetPopUp(filteredSheetDTO, topLeft, bottomRight);
            }
        } else {
            throw new IOException("Failed to filter sheet, response code: " + responseCode);
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
        CellDTO cell = getCellDTOFromServer(Cell.getCellIDFromRowCol(row, getColumnFromCellID(col)));
        return Double.parseDouble(cell.getEffectiveValue().getValue().toString());
    }

//    public int getNumOfColumnsInGrid() {
//        return engine.getNumOfColumnsInCurrSheet(selectedSheet);
//    }

    public int getNumOfColumnsInGrid() throws IOException {
        // בניית URL עם query parameters עבור הגיליון הנבחר
        String finalUrl = HttpUrl
                .parse(GET_NUM_OF_COLUMNS_IN_GRID_URL) // קבוע עם ה-URL לפונקציה בשרת
                .newBuilder()
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת SheetData ל-JSON ושליחתו בגוף הבקשה
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // קבלת תגובת השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // קבלת מספר העמודות המוחזר מהשרת
                return gson.fromJson(bufferedReader, Integer.class);
            }
        } else {
            throw new IOException("Failed to get number of columns, response code: " + responseCode);
        }
    }




//    public void showDynamicCalculation(String selectedCellId, String orgValue) throws IOException {
//
//        CellValue newCellValue = EngineImpl.convertStringToCellValue(orgValue);
//        mainGridComponentController.createInnerCellsInGrid((SheetDTO) engine.DynamicCalculationOnSheet(selectedCellId, newCellValue, orgValue, selectedSheet));
//    }

    public void showDynamicCalculation(String selectedCellId, String orgValue) throws IOException {
        // בניית URL עם query parameters עבור ה-cellId ו-orgValue
        String finalUrl = HttpUrl
                .parse(DYNAMIC_CALCULATION_URL) // קבוע עם ה-URL לפונקציה בשרת
                .newBuilder()
                .addQueryParameter("cellId", selectedCellId)
                .addQueryParameter("orgValue", orgValue)
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת SheetData ל-JSON ושליחתו בגוף הבקשה
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        // שליחת הנתונים לשרת (SheetData כ-body)
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // קבלת תגובת השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // המרת התגובה ל-SheetDTO והצגת החישוב הדינמי
                SheetDTO sheetDTO = gson.fromJson(bufferedReader, SheetDTO.class);
                mainGridComponentController.createInnerCellsInGrid(sheetDTO);
            }
        } else {
            throw new IOException("Failed to perform dynamic calculation, response code: " + responseCode);
        }
    }


//    public void showCurrentSheetOnGrid() throws IOException {
//        mainGridComponentController.createInnerCellsInGrid((SheetDTO) engine.getSheetDTO(selectedSheet));
//    }

    public void showCurrentSheetOnGrid() throws IOException {
        // בניית URL עבור הבקשה לקבלת הגיליון הנוכחי
        String finalUrl = HttpUrl
                .parse(GET_SHEET_DTO) // קבוע עם ה-URL לפונקציה בשרת
                .newBuilder()
                .build()
                .toString();

        // יצירת חיבור לשרת
        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // המרת SheetData ל-JSON ושליחתו בגוף הבקשה
        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        // שליחת הנתונים לשרת (SheetData כ-body)
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // קבלת תגובת השרת
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                // המרת התגובה ל-SheetDTO והצגת הגיליון על הגריד
                SheetDTO sheetDTO = gson.fromJson(bufferedReader, SheetDTO.class);
                mainGridComponentController.createInnerCellsInGrid(sheetDTO);
            }
        } else {
            throw new IOException("Failed to retrieve sheet data, response code: " + responseCode);
        }
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
        usernameLabel.getStyleClass().add("username-label");
    }
}
