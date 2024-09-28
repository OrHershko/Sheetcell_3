package components.sortandfilter;

import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

public interface ColumnActionController {
    void setChoiceBox(ChoiceBox<String> choiceBox);
    void setGetRangePopUpController(GetRangePopUpController getRangePopUpController);
    void setPopUpStage(Stage stage);
}
