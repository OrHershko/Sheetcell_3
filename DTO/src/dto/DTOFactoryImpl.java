package dto;

import api.DTO;
import api.DTOFactory;
import impl.Range;
import impl.cell.Cell;
import impl.sheet.Sheet;

public class DTOFactoryImpl implements DTOFactory {
    @Override
    public DTO createSheetDTO(Sheet sheet) {
        return new SheetDTO(sheet);
    }

    @Override
    public DTO createCellDTO(Cell cell) {
        return new CellDTO(cell);
    }

    @Override
    public DTO createEmptyCellDTO(String identity) {
        return new CellDTO(identity);
    }

    @Override
    public DTO createRangeDTO(Range range) {
        return new RangeDTO(range);
    }
}