package dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for Sheet information
 * Contains all sheet data needed for communication between client and server
 */
public class SheetDTO implements DTO {
    private Map<String, CellDTO> activeCells;
    private String name;
    private int version;
    private int numOfRows;
    private int numOfCols;
    private int rowHeight;
    private int colWidth;
    private int changedCellsCount;

    // Default constructor for serialization frameworks
    public SheetDTO() {
        this.activeCells = new HashMap<>();
        this.changedCellsCount = 0;
    }

    // Full constructor
    public SheetDTO(String name, int version, int numOfRows, int numOfCols, 
                    int rowHeight, int colWidth, Map<String, CellDTO> activeCells, int changedCellsCount) {
        this.name = name;
        this.version = version;
        this.numOfRows = numOfRows;
        this.numOfCols = numOfCols;
        this.rowHeight = rowHeight;
        this.colWidth = colWidth;
        this.activeCells = activeCells != null ? new HashMap<>(activeCells) : new HashMap<>();
        this.changedCellsCount = changedCellsCount;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getNumOfRows() {
        return numOfRows;
    }

    public void setNumOfRows(int numOfRows) {
        this.numOfRows = numOfRows;
    }

    public int getNumOfCols() {
        return numOfCols;
    }

    public void setNumOfCols(int numOfCols) {
        this.numOfCols = numOfCols;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    public int getColWidth() {
        return colWidth;
    }

    public void setColWidth(int colWidth) {
        this.colWidth = colWidth;
    }

    public Map<String, CellDTO> getActiveCells() {
        return activeCells;
    }

    public void setActiveCells(Map<String, CellDTO> activeCells) {
        this.activeCells = activeCells != null ? activeCells : new HashMap<>();
    }

    public int getChangedCellsCount() {
        return changedCellsCount;
    }

    public void setChangedCellsCount(int changedCellsCount) {
        this.changedCellsCount = changedCellsCount;
    }
}
