package components.commands;

import components.maingrid.cell.CellComponentController;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.AppController;

import java.util.Optional;

import static main.AppController.showErrorDialog;

public class CommandsComponentController {
    @FXML
    private Button setRowHeightButton;

    @FXML
    private Button setColWidthButton;

    @FXML
    private Button setColAlignmentButton;

    @FXML
    private Button designCell;

    private AppController appController;

    @FXML
    private void initialize() {
        setColWidthButton.setDisable(true);
        setRowHeightButton.setDisable(true);
        setColAlignmentButton.setDisable(true);
        designCell.setDisable(true);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void disableButtons(boolean disable) {
        setRowHeightButton.setDisable(disable);
        setColWidthButton.setDisable(disable);
        setColAlignmentButton.setDisable(disable);
        designCell.setDisable(disable);
    }

    @FXML
    public void designCellOnClick() {
        Optional<String> result = getCellIDFromUser();

        if (result.isPresent()) {
            String cellId = result.get().toUpperCase(); 

            CellComponentController cell = appController.getCellControllerById(cellId);

            if (cell != null) {
                Stage styleStage = new Stage();
                styleStage.setTitle("Set Style for Cell " + cellId);

                ColorPicker backgroundColorPicker = new ColorPicker();
                ColorPicker textColorPicker = new ColorPicker();

                Button applyButton = createApplyButton(backgroundColorPicker, cell, textColorPicker, styleStage);
                Button resetButton = createResetButton(cell, styleStage);

                VBox vbox = new VBox(10);
                vbox.getChildren().addAll(new Label("Background Color:"), backgroundColorPicker,
                        new Label("Text Color:"), textColorPicker,
                        applyButton, resetButton);

                Scene scene = new Scene(vbox, 300, 200);
                styleStage.setScene(scene);

                styleStage.initModality(Modality.APPLICATION_MODAL);

                styleStage.showAndWait();
            }
            else {
                AppController.showErrorDialog("Error", "The cell ID you entered does not exist.");
            }
        }
    }

    private static Button createResetButton(CellComponentController cell, Stage styleStage) {
        Button resetButton = new Button("Reset Cell Style");
        resetButton.setOnAction(event -> {
            cell.getCellLabel().setStyle(""); 
            styleStage.close();
        });
        return resetButton;
    }

    private Button createApplyButton(ColorPicker backgroundColorPicker, CellComponentController cell, ColorPicker textColorPicker, Stage styleStage) {
        Button applyButton = new Button("Apply Colors");
        applyButton.setOnAction(event -> {
            String backgroundColor = toRgbString(backgroundColorPicker.getValue());
            cell.getCellLabel().setStyle("-fx-background-color: " + backgroundColor + ";");

            String textColor = toRgbString(textColorPicker.getValue());
            cell.getCellLabel().setStyle(cell.getCellLabel().getStyle() + "-fx-text-fill: " + textColor + ";");

            styleStage.close();
        });
        return applyButton;
    }

    private static Optional<String> getCellIDFromUser() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Select Cell and Set Style");

        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Select Cell");
        inputDialog.setHeaderText("Enter the cell ID (e.g., A1, B2) to modify:");
        inputDialog.setContentText("Cell ID:");

        return inputDialog.showAndWait();
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    @FXML
    private void setRowsHeightOnClick() {
        TextInputDialog rowDialog = new TextInputDialog();
        rowDialog.setTitle("Select Row");
        rowDialog.setHeaderText("Select the row to change height");
        rowDialog.setContentText("Please enter the row number:");

        rowDialog.showAndWait().ifPresent(rowInput -> {
            try {
                int rowIndex = Integer.parseInt(rowInput);

                if (rowIndex >= 1 && appController.checkIfRowExist(rowIndex)) {
                    TextInputDialog heightDialog = new TextInputDialog();
                    heightDialog.setTitle("Set Row Height");
                    heightDialog.setHeaderText("Set the height for row " + rowIndex);
                    heightDialog.setContentText("Please enter the desired row height:");

                    heightDialog.showAndWait().ifPresent(heightInput -> {
                        try {
                            int height = Integer.parseInt(heightInput);

                            if (height > 0) {
                                appController.setRowHeightInGrid(rowIndex + 1, height); 
                            }
                            else {
                                showErrorDialog("Invalid input", "Row height must be a positive number.");
                            }
                        }
                        catch (NumberFormatException e) {
                            showErrorDialog("Invalid input", "Please enter a valid number for row height.");
                        }
                    });
                }
                else {
                    showErrorDialog("Invalid input", "The entered row number is out of range.");
                }
            }
            catch (NumberFormatException e) {
                showErrorDialog("Invalid input", "Please enter a valid row number.");
            }
        });
    }


    @FXML
    private void setColsWidthOnClick() {
        TextInputDialog colDialog = new TextInputDialog();
        colDialog.setTitle("Select Column");
        colDialog.setHeaderText("Select the column to change width");
        colDialog.setContentText("Please enter the column letter (A, B, C, ...):");

        colDialog.showAndWait().ifPresent(colInput -> {
            String columnLetter = colInput.toUpperCase();

            if (columnLetter.length() == 1 && columnLetter.charAt(0) >= 'A' && columnLetter.charAt(0) <= 'Z') {
                int colIndex = columnLetter.charAt(0) - 'A' + 1;

                if (appController.checkIfColExist(colIndex)) {
                    TextInputDialog widthDialog = new TextInputDialog();
                    widthDialog.setTitle("Set Column Width");
                    widthDialog.setHeaderText("Set the width for column " + columnLetter);
                    widthDialog.setContentText("Please enter the desired column width:");

                    widthDialog.showAndWait().ifPresent(widthInput -> {
                        try {
                            int width = Integer.parseInt(widthInput);

                            if (width > 0) {
                                appController.setColWidthInGrid(colIndex + 1, width); 
                            } else {
                                showErrorDialog("Invalid input", "Column width must be a positive number.");
                            }
                        } catch (NumberFormatException e) {
                            showErrorDialog("Invalid input", "Please enter a valid number for column width.");
                        }
                    });
                } else {
                    showErrorDialog("Invalid input", "The entered column letter is out of range.");
                }
            } else {
                showErrorDialog("Invalid input", "Please enter a valid column letter.");
            }
        });
    }

    @FXML
    public void setColsAlignmentOnClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Columns Alignment");
        dialog.setHeaderText("Set the alignment for the columns");
        dialog.setContentText("Please enter the column name (e.g., A, B, C):");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String columnName = result.get().toUpperCase();

            ChoiceDialog<String> alignmentDialog = new ChoiceDialog<>("Alignment", "Left", "Center", "Right");
            alignmentDialog.setTitle("Select Alignment");
            alignmentDialog.setHeaderText("Choose the alignment for column " + columnName);
            alignmentDialog.setContentText("Select alignment:");

            Optional<String> alignmentResult = alignmentDialog.showAndWait();
            if (alignmentResult.isPresent()) {
                String alignment = alignmentResult.get();

                int columnIndex = columnName.charAt(0) - 'A' + 1; 

                appController.updateColumnAlignment(columnIndex, alignment);
            }
        }
    }


}
