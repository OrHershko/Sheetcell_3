package dto;

/**
 * Data Transfer Object for Sheet metadata
 * Contains lightweight sheet information for listings and dashboards
 */
public class SheetMetadataDTO {
    private String id;
    private String name;
    private String owner;
    private int numOfRows;
    private int numOfCols;
    private PermissionDTO.PermissionType userPermission;
    private long lastModified;
    private int version;

    // Default constructor for serialization frameworks
    public SheetMetadataDTO() {
    }

    // Full constructor
    public SheetMetadataDTO(String id, String name, String owner, int numOfRows, int numOfCols,
                           PermissionDTO.PermissionType userPermission, long lastModified, int version) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.numOfRows = numOfRows;
        this.numOfCols = numOfCols;
        this.userPermission = userPermission;
        this.lastModified = lastModified;
        this.version = version;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public PermissionDTO.PermissionType getUserPermission() {
        return userPermission;
    }

    public void setUserPermission(PermissionDTO.PermissionType userPermission) {
        this.userPermission = userPermission;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
} 