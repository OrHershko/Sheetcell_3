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

import java.io.IOException;
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


    public void setAppController(AppController appController) {
        this.appController = appController;
    }


    @FXML
    public void designCellOnClick() {
        Optional<String> result = getCellIDFromUser();

        if (result.isPresent()) {
            String cellId = result.get().toUpperCase(); // מקבל את שם התא

            // קבלת CellComponentController מה-AppController
            CellComponentController cell = appController.getCellControllerById(cellId);

            if (cell != null) {
                // אם התא קיים, הצגת פופ-אפ לבחירת צבעים
                Stage styleStage = new Stage();
                styleStage.setTitle("Set Style for Cell " + cellId);

                // יצירת ColorPickers עבור צבע רקע וצבע טקסט
                ColorPicker backgroundColorPicker = new ColorPicker();
                ColorPicker textColorPicker = new ColorPicker();

                Button applyButton = createApplyButton(backgroundColorPicker, cell, textColorPicker, styleStage);
                Button resetButton = createResetButton(cell, styleStage);

                // הוספת כל הרכיבים לפריסת VBox
                VBox vbox = new VBox(10);
                vbox.getChildren().addAll(new Label("Background Color:"), backgroundColorPicker,
                        new Label("Text Color:"), textColorPicker,
                        applyButton, resetButton);

                // הגדרת הפריסה של החלון
                Scene scene = new Scene(vbox, 300, 200);
                styleStage.setScene(scene);

                // הגדרת החלון כמודאלי (modal)
                styleStage.initModality(Modality.APPLICATION_MODAL);

                // הצגת הפופ-אפ
                styleStage.showAndWait();
            }
            else {
                AppController.showErrorDialog("Error", "The cell ID you entered does not exist.");
            }
        }
    }

    private static Button createResetButton(CellComponentController cell, Stage styleStage) {
        // כפתור לאיפוס הסגנון של התא
        Button resetButton = new Button("Reset Cell Style");
        resetButton.setOnAction(event -> {
            cell.getCellLabel().setStyle(""); // איפוס העיצוב
            styleStage.close();
        });
        return resetButton;
    }

    private Button createApplyButton(ColorPicker backgroundColorPicker, CellComponentController cell, ColorPicker textColorPicker, Stage styleStage) {
        // כפתור להחלת צבעים
        Button applyButton = new Button("Apply Colors");
        applyButton.setOnAction(event -> {
            // קביעת צבע רקע
            String backgroundColor = toRgbString(backgroundColorPicker.getValue());
            cell.getCellLabel().setStyle("-fx-background-color: " + backgroundColor + ";");

            // קביעת צבע טקסט
            String textColor = toRgbString(textColorPicker.getValue());
            cell.getCellLabel().setStyle(cell.getCellLabel().getStyle() + "-fx-text-fill: " + textColor + ";");

            // סגירת החלון
            styleStage.close();
        });
        return applyButton;
    }

    private static Optional<String> getCellIDFromUser() {
        // יצירת חלון פופ-אפ חדש
        Stage popupStage = new Stage();
        popupStage.setTitle("Select Cell and Set Style");

        // בקשת הזנת שם התא (למשל "A1")
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Select Cell");
        inputDialog.setHeaderText("Enter the cell ID (e.g., A1, B2) to modify:");
        inputDialog.setContentText("Cell ID:");

        // הצגת הדיאלוג וקבלת תוצאת המשתמש
        return inputDialog.showAndWait();
    }

    // פונקציה להמרת צבע לערכי RGB
    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }


    @FXML
    private void setRowsHeightOnClick() {
        // יצירת דיאלוג להזנת מספר השורה
        TextInputDialog rowDialog = new TextInputDialog();
        rowDialog.setTitle("Select Row");
        rowDialog.setHeaderText("Select the row to change height");
        rowDialog.setContentText("Please enter the row number:");

        // הצגת הדיאלוג וחכות לתשובת המשתמש
        rowDialog.showAndWait().ifPresent(rowInput -> {
            try {
                // המרת הקלט ממחרוזת למספר (מספר השורה)
                int rowIndex = Integer.parseInt(rowInput);

                // בדיקה אם השורה קיימת
                if (rowIndex >= 1 && appController.checkIfRowExist(rowIndex)) {
                    // יצירת דיאלוג להזנת גובה השורה
                    TextInputDialog heightDialog = new TextInputDialog();
                    heightDialog.setTitle("Set Row Height");
                    heightDialog.setHeaderText("Set the height for row " + rowIndex);
                    heightDialog.setContentText("Please enter the desired row height:");

                    // הצגת דיאלוג להזנת גובה
                    heightDialog.showAndWait().ifPresent(heightInput -> {
                        try {
                            int height = Integer.parseInt(heightInput);

                            if (height > 0) {
                                appController.setRowHeightInGrid(rowIndex + 1, height); // השורה מתבססת על אינדקס 0
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
                    // הצגת הודעת שגיאה במקרה שהשורה לא קיימת
                    showErrorDialog("Invalid input", "The entered row number is out of range.");
                }
            }
            catch (NumberFormatException e) {
                // הצגת הודעת שגיאה במקרה של קלט לא חוקי
                showErrorDialog("Invalid input", "Please enter a valid row number.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @FXML
    private void setColsWidthOnClick() {
        // יצירת דיאלוג להזנת אות העמודה (A, B, C...)
        TextInputDialog colDialog = new TextInputDialog();
        colDialog.setTitle("Select Column");
        colDialog.setHeaderText("Select the column to change width");
        colDialog.setContentText("Please enter the column letter (A, B, C, ...):");

        // הצגת הדיאלוג וחכות לתשובת המשתמש
        colDialog.showAndWait().ifPresent(colInput -> {
            // המרה לאות גדולה למקרה שהמשתמש הזין אות קטנה
            String columnLetter = colInput.toUpperCase();

            // בדיקת תקינות האות (רק A-Z)
            if (columnLetter.length() == 1 && columnLetter.charAt(0) >= 'A' && columnLetter.charAt(0) <= 'Z') {
                // המרת האות לאינדקס עמודה (A -> 1, B -> 2, וכו')
                int colIndex = columnLetter.charAt(0) - 'A' + 1;

                // בדיקה אם העמודה קיימת
                try {
                    if (appController.checkIfColExist(colIndex)) {
                        // יצירת דיאלוג להזנת רוחב העמודה
                        TextInputDialog widthDialog = new TextInputDialog();
                        widthDialog.setTitle("Set Column Width");
                        widthDialog.setHeaderText("Set the width for column " + columnLetter);
                        widthDialog.setContentText("Please enter the desired column width:");

                        // הצגת דיאלוג להזנת רוחב
                        widthDialog.showAndWait().ifPresent(widthInput -> {
                            try {
                                // המרת הקלט ממחרוזת למספר (רוחב העמודה)
                                int width = Integer.parseInt(widthInput);

                                // בדיקת חוקיות הרוחב
                                if (width > 0) {
                                    appController.setColWidthInGrid(colIndex + 1, width); // השורה מתבססת על אינדקס 0
                                } else {
                                    // הצג הודעת שגיאה אם הערך אינו תקין
                                    showErrorDialog("Invalid input", "Column width must be a positive number.");
                                }
                            } catch (NumberFormatException e) {
                                // הצגת הודעת שגיאה במקרה של קלט לא חוקי
                                showErrorDialog("Invalid input", "Please enter a valid number for column width.");
                            }
                        });
                    } else {
                        // הצגת הודעת שגיאה במקרה שהעמודה לא קיימת
                        showErrorDialog("Invalid input", "The entered column letter is out of range.");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // הצגת הודעת שגיאה במקרה שהקלט לא תקין
                showErrorDialog("Invalid input", "Please enter a valid column letter.");
            }
        });
    }

    @FXML
    public void setColsAlignmentOnClick() {
        // יצירת תיבת טקסט לקבלת שם העמודה
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Columns Alignment");
        dialog.setHeaderText("Set the alignment for the columns");
        dialog.setContentText("Please enter the column name (e.g., A, B, C):");

        // הצגת הדיאלוג וחילוץ הערך שהמשתמש הכניס
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String columnName = result.get().toUpperCase();

            // יצירת תיבת בחירה (ComboBox) עבור יישור העמודה
            ChoiceDialog<String> alignmentDialog = new ChoiceDialog<>("Alignment", "Left", "Center", "Right");
            alignmentDialog.setTitle("Select Alignment");
            alignmentDialog.setHeaderText("Choose the alignment for column " + columnName);
            alignmentDialog.setContentText("Select alignment:");

            // הצגת תיבת הבחירה וחילוץ הבחירה
            Optional<String> alignmentResult = alignmentDialog.showAndWait();
            if (alignmentResult.isPresent()) {
                String alignment = alignmentResult.get();

                // המרת שם העמודה לאינדקס העמודה
                int columnIndex = columnName.charAt(0) - 'A' + 1; // הנחת שהעמודה היא A, B, C וכו'

                // עדכון היישור של כל התאים בעמודה שנבחרה
                appController.updateColumnAlignment(columnIndex, alignment);
            }
        }
    }


}
