package impl;

import dto.DTO;
import api.DTOFactory;
import dto.*;
import impl.cell.Cell;
import impl.sheet.Sheet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating Data Transfer Objects from domain objects
 * Located in engine module to access domain classes while keeping DTOs clean
 */
public class DTOFactoryImpl implements DTOFactory {

    @Override
    public DTO createSheetDTO(Sheet sheet) {
        Map<String, CellDTO> activeCellsDTO = new HashMap<>();
        
        // Convert active cells to DTOs
        for (Map.Entry<String, Cell> entry : sheet.getActiveCells().entrySet()) {
            activeCellsDTO.put(entry.getKey(), createCellDTOFromCell(entry.getValue()));
        }

        return new SheetDTO(
            sheet.getName(),
            sheet.getVersion(),
            sheet.getNumOfRows(),
            sheet.getNumOfCols(),
            sheet.getRowHeight(),
            sheet.getColWidth(),
            activeCellsDTO,
            sheet.getChangedCellsCount()
        );
    }

    @Override
    public DTO createCellDTO(Cell cell) {
        return createCellDTOFromCell(cell);
    }

    @Override
    public DTO createEmptyCellDTO(String identity) {
        return new CellDTO(identity);
    }

    @Override
    public DTO createRangeDTO(Range range) {
        // Convert range cells to DTOs
        java.util.List<CellDTO> cellDTOs = new java.util.ArrayList<>();
        for (Cell cell : range.getCells()) {
            cellDTOs.add(createCellDTOFromCell(cell));
        }

        return new RangeDTO(
            range.getName(),
            range.getTopLeft(),
            range.getBottomRight(),
            cellDTOs
        );
    }

    private CellDTO createCellDTOFromCell(Cell cell) {
        // Convert cell dependencies to string sets
        Set<String> influencing = new HashSet<>();
        Set<String> dependentOn = new HashSet<>();
        
        if (cell.getCellsImInfluencing() != null) {
            for (Cell influencedCell : cell.getCellsImInfluencing()) {
                influencing.add(influencedCell.getIdentity());
            }
        }
        
        if (cell.getCellsImDependentOn() != null) {
            for (Cell dependencyCell : cell.getCellsImDependentOn()) {
                dependentOn.add(dependencyCell.getIdentity());
            }
        }

        return new CellDTO(
            cell.getIdentity(),
            cell.getVersion(),
            cell.getEffectiveValue() != null ? cell.getEffectiveValue().getValue().toString() : "",
            cell.getOriginalValue(),
            influencing,
            dependentOn,
            cell.getUsernameOfUpdater()
        );
    }
} 