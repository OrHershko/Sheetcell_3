package components.ranges;

import exception.RangeDoesntExistException;
import exception.RangeUsedInFunctionException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import main.AppController;

import java.io.IOException;
import java.util.InputMismatchException;

public class RangesController {

    @FXML
    private Button addNewRangeButton;

    @FXML
    private Button deleteRangeButton;

    @FXML
    private Button markRangeButton;

    @FXML
    private Button unmarkRangeButton;

    private AppController appController;



    @FXML
    private void addNewRangeOnClick() {
        // בקשת שם ה-Range
        String rangeName = requestRangeName();
        if (rangeName == null) {
            return; // המשתמש לחץ על ביטול
        }

        // בקשת התא השמאלי העליון
        String topLeftCell = requestCellPosition("Top-left cell", "Define the top-left cell of the range (e.g., A1):");
        if (topLeftCell == null) {
            return; // המשתמש לחץ על ביטול
        }

        // בקשת התא הימני התחתון
        String bottomRightCell = requestCellPosition("Bottom-right cell", "Define the bottom-right cell of the range (e.g., A4):");
        if (bottomRightCell == null) {
            return; // המשתמש לחץ על ביטול
        }

        topLeftCell = topLeftCell.toUpperCase();
        bottomRightCell = bottomRightCell.toUpperCase();
        // אימות והגדרת הטווח
        try {
            if(!isValidCellId(topLeftCell) || !isValidCellId(bottomRightCell)) {
                throw new InputMismatchException("Invalid cell identity, Please enter a cell in the right format (e.g., A4).");
            }
            appController.addNewRange(topLeftCell,bottomRightCell,rangeName);

        } catch (Exception e) {
            // הצגת הודעת שגיאה במקרה של קלט לא חוקי
            AppController.showErrorDialog("Invalid Input", e.getMessage());
        }
    }

    // פונקציה לבדוק אם הזנת תא תקינה
    private boolean isValidCellId(String cellId) {
        // בדיקה אם תא תקין (למשל, A1, B2)
        return cellId.matches("^[A-Z]+[1-9][0-9]*$");
    }

    // פונקציה לבקשת שם ה-Range
    private String requestRangeName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Range Name");
        dialog.setHeaderText("Define a unique name for the new range");
        dialog.setContentText("Please enter the range name:");

        return dialog.showAndWait().orElse(null); // מחזירה את השם שהוזן או null אם המשתמש ביטל
    }

    // פונקציה לבקשת מיקום תא
    public static String requestCellPosition(String title, String headerText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText("Please enter the cell position:");

        return dialog.showAndWait().orElse(null); // מחזירה את הקלט או null אם המשתמש ביטל
    }


    @FXML
    private void markExistingRangeOnClick(){
        String rangeName = requestRangeName();
        if (rangeName == null) {
            return; // המשתמש לחץ על ביטול או לא הזין שם
        }

        // בדיקה אם הטווח קיים במערכת
        try {
            appController.markCellsInRange(rangeName);
        }
        catch (Exception e) {
            AppController.showErrorDialog("Range not found", "The range '" + rangeName + "' does not exist.");
        }

    }

    @FXML
    private void unmarkExistingRangeOnClick(){
        String rangeName = requestRangeName();
        if (rangeName == null) {
            return;
        }

        try {
            appController.unmarkCellsInRange(rangeName);
        }
        catch (Exception e){
            AppController.showErrorDialog("Range unmark error", e.getMessage());
        }
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void deleteRangeOnClick(){
        String rangeName = requestRangeName();
        if (rangeName == null) {
            return;
        }

        try{
            appController.deleteExistingRange(rangeName);
        }
        catch (RangeDoesntExistException e){
            AppController.showErrorDialog("Range not found", e.getMessage());
        }
        catch (RangeUsedInFunctionException e){
            AppController.showErrorDialog("Range Deleting Error", e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
