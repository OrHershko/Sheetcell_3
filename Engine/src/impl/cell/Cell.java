package impl.cell;

import api.CellValue;
import impl.cell.value.StringValue;
import impl.sheet.Sheet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Cell implements Serializable {
    private final Sheet mySheet;
    private final String identity;
    private CellValue effectiveValue = new StringValue("");
    private String originalValue = "";
    private Set<Cell> cellsImInfluencing = new HashSet<>();
    private Set<Cell> cellsImDependentOn = new HashSet<>();
    private int version = 0;

    public Cell(Sheet sheet, String identity) {
        mySheet = sheet;
        this.identity = identity;
    }

    public Cell(Sheet newSheet, Cell cellToCopy) {
        mySheet = newSheet;
        this.identity = cellToCopy.getIdentity();
        effectiveValue = cellToCopy.getEffectiveValue().clone();
        effectiveValue.setActivatingCell(this);
        originalValue = cellToCopy.getOriginalValue();
        cellsImInfluencing = new HashSet<>(cellToCopy.getCellsImInfluencing());
        cellsImDependentOn = new HashSet<>(cellToCopy.getCellsImDependentOn());
        version = cellToCopy.getVersion();
    }

    public static String getCellIDFromRowCol(int row, int col) {
        return String.format("%C%d", col + 'A' - 1, row);
    }


    public Sheet getSheet() {
        return mySheet;
    }


    public void updateValues(CellValue effectiveValue, String originalValue, boolean isFromFile) {
        effectiveValue.setActivatingCell(this);
        this.effectiveValue = effectiveValue;
        this.originalValue = originalValue;
        version++;
    }

    public int getVersion() {
        return version;
    }

    public CellValue getEffectiveValue() {
        return effectiveValue;
    }

    public void calculateEffectiveValue() {
        effectiveValue.calculateAndSetEffectiveValue();
    }


    public String getOriginalValue() {
        return originalValue;
    }

    public Set<Cell> getCellsImInfluencing() {
        return cellsImInfluencing;
    }

    public Set<Cell> getCellsImDependentOn() {
        return cellsImDependentOn;
    }

    public String getIdentity() {
        return identity;
    }

    public void clearDependenciesLists() {
        cellsImInfluencing.clear();
        cellsImDependentOn.clear();
    }

    public static int getColumnFromCellID(String cellID) {
        return cellID.charAt(0) - 'A' + 1;
    }

    public static int getRowFromCellID(String cellID) {
        return Integer.parseInt(cellID.substring(1));
    }

    public void setCellsImDependentOn(Set<Cell> cellsImDependentOn) {
        this.cellsImDependentOn = cellsImDependentOn;
    }

    public void setCellsImInfluencing(Set<Cell> cellsImInfluencing) {
        this.cellsImInfluencing = cellsImInfluencing;
    }
}
