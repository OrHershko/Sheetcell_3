package components.maingrid.cell;


import components.bonuses.BonusesController;
import dto.CellDTO;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;
import main.AppController;

public class CellComponentController {

    @FXML
    private Label effectiveValue;

    private CellDTO cell;

    private AppController appController;

    public void setCellSize(){
        effectiveValue.setPrefHeight(appController.getPrefRowHeight());
        effectiveValue.setPrefWidth(appController.getPrefColWidth());
    }

    public Label getCellLabel() {
        return effectiveValue;
    }

    public void setEffectiveValue(String effectiveValue) {
        this.effectiveValue.setText(effectiveValue);
    }

    public void setCell(CellDTO cell) {
        this.cell = cell;
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    public void onMouseClicked() {
        cellClickedAnimation();
        appController.displayCellDataOnActionLine(cell);
        appController.colorDependencies(cell.getCellsImDependentOn(),"DependentOn");
        appController.colorDependencies(cell.getCellsImInfluencing(),"Influencing");
    }

    private void cellClickedAnimation() {
        effectiveValue.setScaleX(1);
        effectiveValue.setScaleY(1);
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), effectiveValue);
        scaleTransition.setByX(1.5);
        scaleTransition.setByY(1.5);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);
        if(BonusesController.animationsEnabledProperty.get())
            scaleTransition.play();

    }


}

