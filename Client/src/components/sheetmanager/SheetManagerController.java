package components.sheetmanager;

import com.google.gson.Gson;
import components.sheetmanager.commands.CommandsAreaController;
import components.sheetmanager.commands.PermissionRequestResponseController;
import components.sheetmanager.commands.RequestPermissionPopUpController;
import components.sheetmanager.tables.TablesAreaController;
import impl.sheet.PermissionData;
import impl.sheet.SheetData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.AppController;
import okhttp3.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static main.AppController.createConnection;
import static main.AppController.showErrorDialog;
import static utils.Constants.*;

public class SheetManagerController {

    private AppController appController;

    private String username;

    @FXML
    private VBox tablesArea;

    @FXML
    private TablesAreaController tablesAreaController;

    @FXML
    private AnchorPane commandsArea;

    @FXML
    private CommandsAreaController commandsAreaController;

    public void initialize() {
        tablesAreaController.setSheetManagerController(this);
        commandsAreaController.setSheetManagerController(this);
    }

    public void startMainApp() {
        URL appPage = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        Platform.runLater(() -> {
            try {
                Stage primaryStage = (Stage) tablesArea.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(appPage);
                Parent root = fxmlLoader.load();
                appController = fxmlLoader.getController();
                appController.setNewSelectedSheet(tablesAreaController.getSelectedSheet());
                appController.setUsername(username);
                Scene scene = new Scene(root,1200,600);
                appController.applySkin("default");
                primaryStage.setTitle("Sheetcell");
                primaryStage.setScene(scene);
                primaryStage.centerOnScreen();
                primaryStage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
                showErrorDialog("Failed to load app", e.getMessage());
            }
        });
    }

    public void setUsername(String username) {
        this.username = username;
        tablesAreaController.setUsername(username);
    }

    public String getUsername() {
        return username;
    }

    public boolean isPermittedToViewSheet() {
        return tablesAreaController.isPermittedToViewSheet();
    }

    public void showPermissionErrorDialog() {
        showErrorDialog("Permission error", "You don't have permission to view this sheet");
    }

    public void showRequestPermissionPopUp() {
        SheetData selectedSheet = tablesAreaController.getSelectedSheet();
        if (selectedSheet != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(REQUEST_PERMISSION_FXML_RESOURCE_LOCATION));
            try {
                Parent root = fxmlLoader.load();
                RequestPermissionPopUpController requestPermissionPopUpController = fxmlLoader.getController();
                requestPermissionPopUpController.setSheetManagerController(this);
                requestPermissionPopUpController.setSheetData(selectedSheet);
                Stage stage = new Stage();
                stage.setTitle("Request Permission");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog("Failed to load request permission pop up", e.getMessage());
            }
        } else {
            showErrorDialog("No sheet selected", "Please select a sheet to request permission for");
        }
    }

    public void showPermissionRequestResponsePopUp() {
        SheetData selectedSheet = tablesAreaController.getSelectedSheet();
        if (selectedSheet != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PERMISSION_REQUEST_RESPONSE_FXML_RESOURCE_LOCATION));
            try {
                Parent root = fxmlLoader.load();
                PermissionRequestResponseController permissionRequestResponseController = fxmlLoader.getController();
                permissionRequestResponseController.setSheetManagerController(this);
                permissionRequestResponseController.loadPermissionRequests();
                Stage stage = new Stage();
                stage.setTitle("Permission Requests");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog("Failed to load permission request response pop up", e.getMessage());
            }
        } else {
            showErrorDialog("No sheet selected", "Please select a sheet to view permission requests for");
        }
    }

    public void createPermissionRequest(SheetData selectedSheet, String requestType) throws IOException {
        // בניית URL עם query parameters באמצעות HttpUrl
        String finalUrl = HttpUrl
                .parse(ADD_PERMISSION)
                .newBuilder()
                .addQueryParameter("requestType", requestType.toUpperCase())
                .addQueryParameter("username", username)
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
        else{
            tablesAreaController.refreshPermissionTable();
        }
    }

    public List<PermissionData> getPermissionRequests() {
        return tablesAreaController.getSelectedSheet().getPermissionData();
    }

    public void handlePermissionRequest(PermissionData permission, String requestURL) throws IOException {
        // בניית URL עם query parameters באמצעות HttpUrl
        String finalUrl = HttpUrl
                .parse(requestURL)
                .newBuilder()
                .addQueryParameter("permissionType", permission.getPermissionType())
                .addQueryParameter("username", username)
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
        String sheetDataJson = gson.toJson(tablesAreaController.getSelectedSheet());

        // המרת JsonObject למחרוזת JSON
        byte[] postDataBytes = sheetDataJson.getBytes(StandardCharsets.UTF_8);

        // שליחת הנתונים לשרת (SheetData כ-body)
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postDataBytes);
        }

        // בדיקת קוד התגובה של השרת
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to update permission in sheet, response code: " + responseCode);
        }
        else{
            tablesAreaController.refreshPermissionTable();
        }
    }
}
