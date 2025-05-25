package dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Range information
 * Contains range data needed for communication between client and server
 */
public class RangeDTO implements DTO {
    private String name;
    private String topLeft;
    private String bottomRight;
    private List<CellDTO> cells;

    // Default constructor for serialization frameworks
    public RangeDTO() {
        this.cells = new ArrayList<>();
    }

    // Full constructor
    public RangeDTO(String name, String topLeft, String bottomRight, List<CellDTO> cells) {
        this.name = name;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.cells = cells != null ? new ArrayList<>(cells) : new ArrayList<>();
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(String topLeft) {
        this.topLeft = topLeft;
    }

    public String getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(String bottomRight) {
        this.bottomRight = bottomRight;
    }

    public List<CellDTO> getCells() {
        return cells;
    }

    public void setCells(List<CellDTO> cells) {
        this.cells = cells != null ? cells : new ArrayList<>();
    }
}
