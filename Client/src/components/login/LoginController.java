package components.login;

import components.sheetmanager.SheetManagerController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.AppController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.Constants;
import utils.http.HttpClientUtil;

import java.io.IOException;
import java.net.URL;

import static utils.Constants.SHEET_MANAGER_FXML_RESOURCE_LOCATION;

public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private Button loginButton;


    @FXML
    private void loginButtonClicked() {
        String username = usernameTextField.getText();
        if (username.isEmpty()) {
            Platform.runLater(() ->
                    AppController.showErrorDialog("Failed to login", "User name is empty. You can't login with empty user name")
            );
            return;
        }

        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", username)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        AppController.showErrorDialog("Failed to login", e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() ->
                            AppController.showErrorDialog("Failed to login", responseBody)
                    );
                }
                else {
                    showAppInsteadOfLogin();
                }
            }

            private void showAppInsteadOfLogin() {
                URL appPage = getClass().getResource(SHEET_MANAGER_FXML_RESOURCE_LOCATION);
                Platform.runLater(() -> {
                    try {
                        Stage primaryStage = (Stage) loginButton.getScene().getWindow();
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(appPage);
                        Parent root = fxmlLoader.load();
                        SheetManagerController sheetManagerController = fxmlLoader.getController();
                        Scene scene = new Scene(root);
                        primaryStage.setTitle("Sheetcell");
                        primaryStage.setScene(scene);
                        primaryStage.sizeToScene();
                        primaryStage.centerOnScreen();
                        primaryStage.show();
                        sheetManagerController.setUsername(username);
                    } catch (IOException e) {
                        e.printStackTrace();
                        AppController.showErrorDialog("Failed to load app", e.getMessage());
                    }
                });
            }
        });
    }
}
