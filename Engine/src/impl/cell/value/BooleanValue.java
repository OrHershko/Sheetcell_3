package impl.cell.value;

import api.CellValue;
import impl.cell.Cell;

public class BooleanValue implements CellValue {
    private Boolean value;
    private Cell activatingCell;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return Boolean.toString(value).toUpperCase();
    }

    @Override
    public Boolean eval() {
        return value;
    }

    @Override
    public void setActivatingCell(Cell cell) {
        this.activatingCell = cell;
    }

    @Override
    public void calculateAndSetEffectiveValue(){
        value = eval();
    }

    @Override
    public Cell getActivatingCell() {
        return activatingCell;
    }

    @Override
    public BooleanValue clone() {
        return new BooleanValue(value);
    }
}
