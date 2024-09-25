package dto;

import api.CellValue;
import api.DTO;
import impl.cell.Cell;
import impl.cell.value.StringValue;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CellDTO implements DTO {
    private final String identity;
    private final int version;
    private final CellValue effectiveValue;
    private final String originalValue;
    private final Set<String> cellsImInfluencing;
    private final Set<String> cellsImDependentOn;



    public CellDTO(String identity){
        this.version = 0;
        this.effectiveValue = new StringValue("");
        this.originalValue = "";
        this.identity = identity;
        cellsImInfluencing = new HashSet<>();
        cellsImDependentOn = new HashSet<>();
    }

    public CellDTO(Cell cell) {
        identity = cell.getIdentity();
        version = cell.getVersion();
        effectiveValue = cell.getEffectiveValue();
        originalValue = cell.getOriginalValue();
        cellsImInfluencing = cell.getCellsImInfluencing().stream().map(Cell::getIdentity).collect(Collectors.toSet());
        cellsImDependentOn = cell.getCellsImDependentOn().stream().map(Cell::getIdentity).collect(Collectors.toSet());
    }


    public int getVersion() {
        return version;
    }

    public CellValue getEffectiveValue() {
        return effectiveValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public Set<String> getCellsImInfluencing() {
        return cellsImInfluencing;
    }

    public Set<String> getCellsImDependentOn() {
        return cellsImDependentOn;
    }

    public String getIdentity() {
        return identity;
    }
}
