package api;

import impl.cell.Cell;

import java.io.Serializable;

public interface CellValue extends Serializable {
    Object getValue();
    Object eval();
    void setActivatingCell(Cell cell);
    void calculateAndSetEffectiveValue();
    Cell getActivatingCell();
    CellValue clone();
}

