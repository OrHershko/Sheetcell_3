package dto;

import api.DTO;
import impl.Range;
import impl.cell.Cell;

import java.util.ArrayList;
import java.util.List;

public class RangeDTO implements DTO {
    private final String name;
    private final String topLeft;
    private final String bottomRight;
    private final List<CellDTO> cells;

    RangeDTO(Range range) {
        this.name = range.getName();
        this.topLeft = range.getTopLeft();
        this.bottomRight = range.getBottomRight();
        this.cells = createCellsDTOList(range.getCells());
    }

    private List<CellDTO> createCellsDTOList(List<Cell> cells) {
        List<CellDTO> cellDTOs = new ArrayList<>();
        for (Cell cell : cells) {
            cellDTOs.add(new CellDTO(cell));
        }
        return cellDTOs;
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

    public List<CellDTO> getCells() {
        return cells;
    }
}
