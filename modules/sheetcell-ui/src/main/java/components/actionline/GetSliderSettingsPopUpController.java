package components.actionline;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.AppController;

import java.io.IOException;

public class GetSliderSettingsPopUpController {

    @FXML
    private TextField minimumValueTextBox;

    @FXML
    private TextField maximumValueTextBox;

    @FXML
    private TextField stepSizeTextBox;




    private ActionLineController actionLineController;

    private Stage currentPopupStage;


    public void setActionLineController(ActionLineController actionLineController) {
        this.actionLineController = actionLineController;
    }

    public void setCurrentPopupStage(Stage currentPopupStage) {
        this.currentPopupStage = currentPopupStage;
    }

    @FXML
    private void nextButtonOnClick() {
        try {
            int minValue = Integer.parseInt(minimumValueTextBox.getText());
            int maxValue = Integer.parseInt(maximumValueTextBox.getText());
            int stepSize = Integer.parseInt(stepSizeTextBox.getText());

            if (minValue >= maxValue) {
                AppController.showErrorDialog("Input Error", "Minimum value must be less than maximum value.");
                return;
            }

            Stage sliderPopupStage = new Stage();
            sliderPopupStage.setTitle("Dynamic Calculation Slider");

            Slider slider = createSlider(minValue, maxValue, stepSize);

            Label sliderValueLabel = new Label("Current Value: " + minValue);
            slider.valueProperty().addListener((observable, oldValue, newValue) -> {
                sliderValueLabel.setText("Current Value: " + newValue.intValue());
                try {
                    updateSelectedCellWithSliderValue(newValue.intValue());
                }
                catch (IOException ignored) {}
            });

            showSliderPopUp(slider, sliderValueLabel, sliderPopupStage);

        } catch (NumberFormatException e) {
            AppController.showErrorDialog("Input Error", "Please enter valid numbers for minimum value, maximum value, and step size.");
        }
    }

    private void showSliderPopUp(Slider slider, Label sliderValueLabel, Stage sliderPopupStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(slider, sliderValueLabel);

        Scene scene = new Scene(layout, 500, 200);
        sliderPopupStage.setScene(scene);

        if(currentPopupStage != null)
        {
            currentPopupStage.close();
        }

        sliderPopupStage.setOnCloseRequest(event -> {
            try {
                actionLineController.showOriginalGrid();
            } catch (IOException ignored) {}
        });

        sliderPopupStage.show();
    }

    private Slider createSlider(int minValue, int maxValue, int stepSize) {
        Slider slider = new Slider(minValue, maxValue, minValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(stepSize);
        slider.setBlockIncrement(stepSize);
        return slider;
    }

    private void updateSelectedCellWithSliderValue(int newValue) throws IOException {

        actionLineController.updateCellDataToEngine(String.valueOf(newValue));
    }

}
