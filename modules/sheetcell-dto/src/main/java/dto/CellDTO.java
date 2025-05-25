package dto;

import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object for Cell information
 * Contains all cell data needed for communication between client and server
 */
public class CellDTO implements DTO {
    private String identity;
    private int version;
    private String effectiveValue;
    private String originalValue;
    private Set<String> cellsImInfluencing;
    private Set<String> cellsImDependentOn;
    private String usernameOfUpdater;

    // Default constructor for serialization frameworks
    public CellDTO() {
        this.cellsImInfluencing = new HashSet<>();
        this.cellsImDependentOn = new HashSet<>();
    }

    // Constructor for empty cell
    public CellDTO(String identity) {
        this.identity = identity;
        this.version = 0;
        this.effectiveValue = "";
        this.originalValue = "";
        this.cellsImInfluencing = new HashSet<>();
        this.cellsImDependentOn = new HashSet<>();
        this.usernameOfUpdater = "";
    }

    // Full constructor
    public CellDTO(String identity, int version, String effectiveValue, String originalValue, 
                   Set<String> cellsImInfluencing, Set<String> cellsImDependentOn, String usernameOfUpdater) {
        this.identity = identity;
        this.version = version;
        this.effectiveValue = effectiveValue;
        this.originalValue = originalValue;
        this.cellsImInfluencing = cellsImInfluencing != null ? new HashSet<>(cellsImInfluencing) : new HashSet<>();
        this.cellsImDependentOn = cellsImDependentOn != null ? new HashSet<>(cellsImDependentOn) : new HashSet<>();
        this.usernameOfUpdater = usernameOfUpdater;
    }

    // Getters and setters
    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getEffectiveValue() {
        return effectiveValue;
    }

    public void setEffectiveValue(String effectiveValue) {
        this.effectiveValue = effectiveValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public Set<String> getCellsImInfluencing() {
        return cellsImInfluencing;
    }

    public void setCellsImInfluencing(Set<String> cellsImInfluencing) {
        this.cellsImInfluencing = cellsImInfluencing != null ? cellsImInfluencing : new HashSet<>();
    }

    public Set<String> getCellsImDependentOn() {
        return cellsImDependentOn;
    }

    public void setCellsImDependentOn(Set<String> cellsImDependentOn) {
        this.cellsImDependentOn = cellsImDependentOn != null ? cellsImDependentOn : new HashSet<>();
    }

    public String getUsernameOfUpdater() {
        return usernameOfUpdater;
    }

    public void setUsernameOfUpdater(String usernameOfUpdater) {
        this.usernameOfUpdater = usernameOfUpdater;
    }
}
