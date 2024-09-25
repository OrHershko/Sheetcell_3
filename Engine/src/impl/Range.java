package impl;

import impl.cell.Cell;
import impl.sheet.Sheet;

import java.io.Serializable;
import java.util.List;

public class Range implements Serializable {
    private final String name;
    private final String topLeft;
    private final String bottomRight;
    private final List<Cell> cells;

    public Range(String name, String topLeft, String bottomRight, Sheet sheet) {
        this.name = name;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.cells = sheet.getCellsInRange(topLeft, bottomRight);
    }

    public String getName() {
        return name;
    }

    public String getTopLeft() {
        return topLeft;
    }

    public String getBottomRight() {
        return bottomRight;
    }

    public List<Cell> getCells() {
        return cells;
    }
}
