package main;

import api.CellValue;
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
import dto.DTO;
import dto.RangeDTO;
import dto.SheetDTO;
import impl.cell.Cell;
import static impl.cell.Cell.getColumnFromCellID;
import static impl.cell.Cell.getRowFromCellID;
import impl.sheet.SheetData;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import static utils.Constants.*;
import utils.Permissions;


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
    private Label usernameLabel;

    @FXML
    private Button backButton;

    @FXML
    private GraphComponentController graphComponentController;

    @FXML
    private Button updateVersionButton;

    private int currentVersionDisplayed;

    private Stage sheetPopUpStage;  // Singleton instance for the popup Stage
    private final IntegerProperty currentPreviousVersion = new SimpleIntegerProperty();  // Property for the previous version number being viewed
    private SheetData selectedSheet;

    private boolean isReadOnly;

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
        
        // Only disable the button if it exists (client-specific)
        if (updateVersionButton != null) {
            updateVersionButton.setDisable(true);
        }
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        startVersionCheck(scheduler);
        try {
            buildSheetPopUpStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startVersionCheck(ScheduledExecutorService scheduler) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int serverVersion = getUpdatedSheetDTOFromServer().getVersion();
                if (serverVersion > currentVersionDisplayed) {
                    Platform.runLater(() -> {
                        if (updateVersionButton != null) {
                            updateVersionButton.setDisable(false);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @FXML
    public void updateVersionOnClick() throws IOException {
        setNewSelectedSheet(selectedSheet);
        if (updateVersionButton != null) {
            updateVersionButton.setDisable(true);
        }
    }

    @FXML
    public void backButtonClicked() throws IOException {
        if (backButton != null && usernameLabel != null) {
            backButton.getScene().getWindow().hide();
            URL appPage = getClass().getResource(SHEET_MANAGER_FXML_RESOURCE_LOCATION);
            LoginController.openSheetManager(appPage, (Stage) backButton.getScene().getWindow(), usernameLabel.getText().replaceAll("Username: ", ""));
        }
    }

    public void displayCellDataOnActionLine(CellDTO cell) {
        actionLineComponentController.displayCellData(cell);
    }

    public void setIsReadOnly(Permissions permission) {
        if(permission == Permissions.READER) {
            isReadOnly = true;
            // TODO: Implement disable methods in UI components
            // actionLineComponentController.disableActionLine(true);
            // commandsComponentController.disableCommandsComponent(true);
            // rangesComponentController.disableRangesComponent(true);
        }
        else{
            isReadOnly = false;
        }
    }

    public void updateCellDataToEngine(String selectedCellId, String newValue) throws IOException {

        if(updateVersionButton != null && !updateVersionButton.isDisable()){
            showErrorDialog("Error", "Unable to apply changes: A newer version of the sheet is available. To make updates, please switch to the latest version.");
            return;
        }

        String username = (usernameLabel != null) ? usernameLabel.getText().replaceAll("Username: ", "") : "DefaultUser";
        
        String finalUrl = HttpUrl
                .parse(UPDATE_CELL)
                .newBuilder()
                .addQueryParameter("cellId", selectedCellId)
                .addQueryParameter("newValue", newValue)
                .addQueryParameter("username", username)
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = createConnection(url);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        byte[] postDataBytes = sheetDataJson.getBytes(StandardCharsets.UTF_8);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(postDataBytes);
        }

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
        currentVersionDisplayed = updatedSheet.getVersion();
        mainGridComponentController.createInnerCellsInGrid(updatedSheet);
        mainGridComponentController.activateMouseClickedOfCell(selectedCellId);
        versionsSelectorComponentController.updateVersionsSelector();
    }

    private SheetDTO getUpdatedSheetDTOFromServer() throws IOException {
        URL url = new URL(GET_SHEET_DTO);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true); 
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CellValue.class, new CellValueAdapter())
                .create();
        String jsonInputString = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

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

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

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

                Gson gson = GSON_INSTANCE;
                return gson.fromJson(bufferedReader, RangeDTO.class);
            }
        } else {
            throw new IOException("Failed to update cells in range, response code: " + responseCode);
        }
    }


    public Map<Integer, DTO> getSheetsPreviousVersionsDTO() throws IOException {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(GET_SHEET_VERSIONS_ENDPOINT)) 
                .url()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST"); 
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet); 
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get previous sheet versions, response code: " + responseCode);
        }

        try (InputStream inputStream = connection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

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

        HttpURLConnection getConnection = buildConnectionToGetRangeData(rangeName);
        RangeDTO rangeDTO = getRangeDTOFromServer(getConnection);

        String finalUrl = HttpUrl
                .parse(DELETE_RANGE_FROM_SHEET)
                .newBuilder()
                .addQueryParameter("rangeName", rangeName)
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection deleteConnection = (HttpURLConnection) url.openConnection();
        deleteConnection.setRequestMethod("POST");
        deleteConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        deleteConnection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = deleteConnection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

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

    public boolean checkRangeOfCells(String topLeft, String bottomRight) throws IOException {
        String finalUrl = HttpUrl
                .parse(CHECK_RANGE_OF_CELLS)
                .newBuilder()
                .addQueryParameter("topLeft", topLeft)
                .addQueryParameter("bottomRight", bottomRight)
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                return gson.fromJson(bufferedReader, Boolean.class);
            }
        } else {
            throw new IOException("Failed to check range of cells, response code: " + responseCode);
        }
    }

    public void sortSheetByColumns(List<String> columnToSortBy, String topLeft, String bottomRight) throws IOException {
        String finalUrl = HttpUrl
                .parse(SORT_SHEET_BY_COLUMNS)
                .newBuilder()
                .addQueryParameter("topLeft", topLeft)
                .addQueryParameter("bottomRight", bottomRight)
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("columnToSortBy", gson.toJsonTree(columnToSortBy));
        jsonObject.add("sheetData", gson.toJsonTree(selectedSheet));

        String jsonInputString = gson.toJson(jsonObject);
        System.out.println("JSON sent to server: " + jsonInputString);

        byte[] postDataBytes = jsonInputString.getBytes(StandardCharsets.UTF_8);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(postDataBytes);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                SheetDTO sortedSheetDTO = gson.fromJson(bufferedReader, SheetDTO.class);

                displaySheetPopUp(sortedSheetDTO, topLeft, bottomRight);
            }
        } else {
            throw new IOException("Failed to sort sheet, response code: " + responseCode);
        }
    }

    public Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight) throws IOException {
        String finalUrl = HttpUrl
                .parse(GET_VALUES_FROM_COLUMN)
                .newBuilder()
                .addQueryParameter("column", column)
                .addQueryParameter("topLeft", topLeft)
                .addQueryParameter("bottomRight", bottomRight)
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                Type setType = new TypeToken<Set<String>>(){}.getType();
                return gson.fromJson(bufferedReader, setType);
            }
        } else {
            throw new IOException("Failed to retrieve values from column, response code: " + responseCode);
        }
    }

    public void filter(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight) throws IOException {
        String finalUrl = HttpUrl
                .parse(FILTER_SHEET_URL) 
                .newBuilder()
                .addQueryParameter("topLeft", topLeft)
                .addQueryParameter("bottomRight", bottomRight)
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        JsonObject requestBody = new JsonObject();
        requestBody.add("colToSelectedValues", gson.toJsonTree(colToSelectedValues));  
        requestBody.add("sheetData", gson.toJsonTree(selectedSheet)); 

        byte[] postDataBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postDataBytes);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                SheetDTO filteredSheetDTO = gson.fromJson(bufferedReader, SheetDTO.class);
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
        CellDTO cell = getCellDTOFromServer(Cell.getCellIDFromRowCol(row, getColumnFromCellID(col)));
        return Double.parseDouble(cell.getEffectiveValue());
    }

    public int getNumOfColumnsInGrid() throws IOException {
        String finalUrl = HttpUrl
                .parse(GET_NUM_OF_COLUMNS_IN_GRID_URL) 
                .newBuilder()
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                return gson.fromJson(bufferedReader, Integer.class);
            }
        } else {
            throw new IOException("Failed to get number of columns, response code: " + responseCode);
        }
    }


    public void showDynamicCalculation(String selectedCellId, String orgValue) throws IOException {
        String finalUrl = HttpUrl
                .parse(DYNAMIC_CALCULATION_URL) 
                .newBuilder()
                .addQueryParameter("cellId", selectedCellId)
                .addQueryParameter("orgValue", orgValue)
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                SheetDTO sheetDTO = gson.fromJson(bufferedReader, SheetDTO.class);
                mainGridComponentController.createInnerCellsInGrid(sheetDTO);
            }
        } else {
            throw new IOException("Failed to perform dynamic calculation, response code: " + responseCode);
        }
    }

    public void showCurrentSheetOnGrid() throws IOException {
        String finalUrl = HttpUrl
                .parse(GET_SHEET_DTO) 
                .newBuilder()
                .build()
                .toString();

        URL url = new URL(finalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        Gson gson = GSON_INSTANCE;
        String sheetDataJson = gson.toJson(selectedSheet);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = sheetDataJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                SheetDTO sheetDTO = gson.fromJson(bufferedReader, SheetDTO.class);
                mainGridComponentController.createInnerCellsInGrid(sheetDTO);
            }
        } else {
            throw new IOException("Failed to retrieve sheet data, response code: " + responseCode);
        }
    }


    public boolean isCellValueNumeric(String cellId) {
        CellDTO cellDTO = getCellDTOFromServer(cellId);
        String effectiveValue = cellDTO.getEffectiveValue();
        try {
            Double.parseDouble(effectiveValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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

            Gson gson = GSON_INSTANCE;
            String sheetDataJson = gson.toJson(selectedSheet);

            byte[] postDataBytes = sheetDataJson.getBytes(StandardCharsets.UTF_8);

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
        currentVersionDisplayed = sheetDTO.getVersion();
        mainGridComponentController.createDynamicGrid(sheetDTO);
        mainGridComponentController.buildGridBoundaries(sheetDTO);
        mainGridComponentController.createInnerCellsInGrid(sheetDTO);
        versionsSelectorComponentController.updateVersionsSelector();
    }

    public void setUsername(String username) {
        if (usernameLabel != null) {
            usernameLabel.setText(usernameLabel.getText() + username);
            usernameLabel.getStyleClass().add("username-label");
        }
    }

    public boolean getIsReadOnly() {
        return isReadOnly;
    }
}
