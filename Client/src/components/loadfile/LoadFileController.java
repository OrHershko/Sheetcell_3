package components.loadfile;

import components.bonuses.BonusesController;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.AppController;

import java.io.File;

public class LoadFileController {

    @FXML
    private TextField filePath;

    @FXML
    private Button loadFileButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    private AppController appController;


    @FXML
    private void loadXmlFile() {
        TranslateTransition translateTransition = createTranslateTransition();
        RotateTransition rotateTransition = createRotateTransition();
        rotateTransition.setOnFinished(event -> openFileChooser());

        if(BonusesController.animationsEnabledProperty.get()){
            translateTransition.play();
            rotateTransition.play();
        }
        else{
            openFileChooser();
        }
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        Stage stage = (Stage) loadFileButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            Task<Void> loadFileTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        for (int i = 0; i <= 100; i++) {
                            updateProgress(i, 100);
                            updateMessage(i + "%");
                            Thread.sleep(5);
                        }
                        appController.loadFileToEngine(selectedFile);
                        return null;
                    } catch (Exception e) {
                        updateProgress(0, 100);
                        throw e;
                    }
                }
            };

            progressBar.progressProperty().bind(loadFileTask.progressProperty());
            progressLabel.textProperty().bind(loadFileTask.messageProperty());

            loadFileTask.setOnSucceeded(event -> filePath.setText(selectedFile.getAbsolutePath()));
            loadFileTask.setOnFailed(event -> showAlert("File Loading Error", loadFileTask.getException().getMessage()));

            new Thread(loadFileTask).start();
        }
    }

    private RotateTransition createRotateTransition() {
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setNode(loadFileButton);
        rotateTransition.setDuration(Duration.millis(500));
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(2);
        rotateTransition.setAutoReverse(true);
        return rotateTransition;
    }

    private TranslateTransition createTranslateTransition() {
        TranslateTransition translateTransition = new TranslateTransition();
        translateTransition.setNode(loadFileButton);
        translateTransition.setDuration(Duration.millis(500));
        translateTransition.setByX(100);
        translateTransition.setCycleCount(2);
        translateTransition.setAutoReverse(true);
        return translateTransition;
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void initialize() {
    progressBar.setProgress(0);
    }
}
