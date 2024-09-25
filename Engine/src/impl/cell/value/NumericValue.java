package impl.cell.value;

import api.CellValue;
import impl.cell.Cell;

public class NumericValue implements CellValue {
    private Double value;
    private Cell activatingCell;

    public NumericValue(double value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        if (value % 1 == 0) {
            return String.format("%,d", value.longValue());
        } else {
            return String.format("%,.2f", value);
        }
    }

    @Override
    public Double eval() {
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
    public NumericValue clone() {
        return new NumericValue(value);
    }
}
