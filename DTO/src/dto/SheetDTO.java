package dto;

import api.DTO;
import impl.cell.Cell;
import impl.sheet.Sheet;

import java.util.HashMap;
import java.util.Map;

public class SheetDTO implements DTO {
    private final Map<String, CellDTO> activeCells;
    private final String name;
    private final int version;
    private final int numOfRows;
    private final int numOfCols;
    private final int rowHeight;
    private final int colWidth;
    private int changedCellsCount = 0;

    public int getChangedCellsCount() {
        return changedCellsCount;
    }

    public SheetDTO(Sheet sheet) {
        name = sheet.getName();
        version = sheet.getVersion();
        numOfRows = sheet.getNumOfRows();
        numOfCols = sheet.getNumOfCols();
        activeCells = createActiveCellDTOMap(sheet);
        rowHeight = sheet.getRowHeight();
        colWidth = sheet.getColWidth();
        changedCellsCount = sheet.getChangedCellsCount();
    }

    private Map<String, CellDTO> createActiveCellDTOMap(Sheet sheet) {
        Map<String, Cell> cellsToCopy = sheet.getActiveCells();
        Map<String, CellDTO> cellDTOMap = new HashMap<>();

        for (Map.Entry<String, Cell> entry : cellsToCopy.entrySet()) {
            CellDTO cellDTO = new CellDTO(entry.getValue());
            cellDTOMap.put(entry.getKey(), cellDTO);
        }

        return cellDTOMap;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public int getNumOfRows() {
        return numOfRows;
    }

    public int getNumOfCols() {
        return numOfCols;
    }

    public float getRowHeight() {return rowHeight;}

    public int getColWidth() {return colWidth;}

    public Map<String,CellDTO> getActiveCells() {
        return activeCells;
    }

}
