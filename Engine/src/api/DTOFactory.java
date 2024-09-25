package api;

import impl.Range;
import impl.cell.Cell;
import impl.sheet.Sheet;

public interface DTOFactory {
    DTO createSheetDTO(Sheet sheet);
    DTO createCellDTO(Cell cell);
    DTO createEmptyCellDTO(String identity);
    DTO createRangeDTO(Range range);
}
