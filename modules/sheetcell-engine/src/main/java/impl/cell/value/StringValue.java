package impl.cell.value;

import api.CellValue;
import impl.cell.Cell;

public class StringValue implements CellValue {
    private String value;
    private Cell activatingCell;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }


    @Override
    public String eval() {
        return this.value;
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
    public StringValue clone() {
        return new StringValue(value);
    }
}
