package components.sheetmanager.tables;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import components.sheetmanager.SheetManagerController;
import dto.SheetDTO;
import impl.sheet.PermissionData;
import impl.sheet.SheetData;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static utils.Constants.*;

public class TablesAreaController {

    @FXML
    private Button loadFileButton;

    @FXML
    private TableView<SheetData> sheetsTable;

    @FXML
    private TableColumn<SheetData, String> usernameColumn;

    @FXML
    private TableColumn<SheetData, String> sheetNameColumn;

    @FXML
    private TableColumn<SheetData, String> sheetSizeColumn;

    @FXML
    private TableColumn<SheetData, String> permissionTypeColumn;

    @FXML
    private TableView<PermissionData> permissionsTable;

    @FXML
    private TableColumn<PermissionData, String> permissionUsernameColumn;

    @FXML
    private TableColumn<PermissionData, String> permissionTypeColumn2;

    @FXML
    private TableColumn<PermissionData, String> permissionStatusColumn;

    @FXML
    private Label usernameLabel;

    private SheetManagerController sheetManagerController;

    public void setSheetManagerController(SheetManagerController sheetManagerController) {
        this.sheetManagerController = sheetManagerController;
    }

    @FXML
    private void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        sheetSizeColumn.setCellValueFactory(new PropertyValueFactory<>("sheetSize"));
        permissionTypeColumn.setCellValueFactory(cellData -> {
            SheetData sheetData = cellData.getValue();
            return new SimpleStringProperty(sheetData.getPermissionTypeForUser(sheetManagerController.getUsername()));
        });
        permissionUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        permissionTypeColumn2.setCellValueFactory(new PropertyValueFactory<>("permissionType"));
        permissionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("permissionStatus"));
        initPermissionTableConnection();
        startSheetDataUpdateTask();
    }

    private void initPermissionTableConnection() {
        sheetsTable.setOnMouseClicked(event -> {
            SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();
            if (selectedSheet != null) {
                List<PermissionData> permissionDataList = getPermissionDataForSheet(selectedSheet);
                updatePermissionTable(permissionDataList);
            }
        });
    }

    private List<PermissionData> getPermissionDataForSheet(SheetData sheetData) {

        return sheetData.getPermissionData();
    }

    private void updatePermissionTable(List<PermissionData> permissionDataList) {
        permissionsTable.getItems().clear();
        permissionsTable.getItems().addAll(permissionDataList);
    }

    @FXML
    private void loadFileOnClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        Stage stage = (Stage) loadFileButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            sendFileToServer(selectedFile);
        }
    }

    private void sendFileToServer(File file) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    HttpURLConnection connection = createConnection(new URL(UPLOAD_FILE));
                    uploadFile(connection, file, sheetManagerController.getUsername());
                    handleServerResponse(connection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=boundary");
        return connection;
    }

    private void uploadFile(HttpURLConnection connection, File file, String username) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);
             FileInputStream fis = new FileInputStream(file)) {

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"username\"\r\n\r\n");
            writer.append(username).append("\r\n");
            writer.flush();

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            writer.append("Content-Type: application/xml\r\n\r\n");
            writer.flush();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();

            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.flush();
        }
    }



    private void handleServerResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Platform.runLater(() -> {
                try {
                    List<SheetData> sheetsDataList = fetchSheetsFromServer();
                    updateTableViewWithSheets(sheetsDataList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            Platform.runLater(() -> {
                System.out.println("Failed to upload file, response code: " + responseCode);
            });
        }
    }

    private void updateTableViewWithSheets(List<SheetData> sheetsDataList) {
        SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();

        sheetsTable.getItems().clear();
        sheetsTable.getItems().addAll(sheetsDataList);

        if (selectedSheet != null) {
            sheetsTable.getSelectionModel().select(selectedSheet);
        }
    }



    private List<SheetData> fetchSheetsFromServer() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(GET_ALL_SHEETS_DATA).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream is = connection.getInputStream();
                 InputStreamReader isr = new InputStreamReader(is)) {

                Gson gson = new Gson();
                Type sheetDataListType = new TypeToken<List<SheetData>>() {}.getType();
                return gson.fromJson(isr, sheetDataListType);
            }
        } else {
            throw new IOException("Failed to fetch sheets from server. Response code: " + responseCode);
        }
    }

    private void startSheetDataUpdateTask() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    List<SheetData> sheetsDataList = fetchSheetsFromServer();

                    Platform.runLater(() -> {
                        updateTableViewWithSheets(sheetsDataList);
                    });

                    Thread.sleep(3000);
                }
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true); // שימוש ב-Thread רקע
        thread.start();
    }

    public SheetData getSelectedSheet() {
        return sheetsTable.getSelectionModel().getSelectedItem();
    }

    public void setUsername(String username) {
        usernameLabel.setText(usernameLabel.getText() + username);
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    }

    public boolean isPermittedToViewSheet() {
        SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();
        return selectedSheet != null && selectedSheet.isPermittedToView(sheetManagerController.getUsername());
    }


    public void refreshPermissionTable() {
        SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();
        if (selectedSheet != null) {
            List<PermissionData> permissionDataList = getPermissionDataForSheet(selectedSheet);
            updatePermissionTable(permissionDataList);
        }
    }
}
