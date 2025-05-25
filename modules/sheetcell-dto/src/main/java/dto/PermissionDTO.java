package dto;

/**
 * Data Transfer Object for Permission information
 * Contains permission data for sheet access control
 */
public class PermissionDTO {
    private String username;
    private String sheetId;
    private PermissionType permission;
    private boolean approved;
    private long requestedAt;
    private long approvedAt;

    public enum PermissionType {
        OWNER, WRITER, READER
    }

    // Default constructor for serialization frameworks
    public PermissionDTO() {
    }

    // Full constructor
    public PermissionDTO(String username, String sheetId, PermissionType permission, 
                        boolean approved, long requestedAt, long approvedAt) {
        this.username = username;
        this.sheetId = sheetId;
        this.permission = permission;
        this.approved = approved;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSheetId() {
        return sheetId;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public PermissionType getPermission() {
        return permission;
    }

    public void setPermission(PermissionType permission) {
        this.permission = permission;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public long getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(long requestedAt) {
        this.requestedAt = requestedAt;
    }

    public long getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(long approvedAt) {
        this.approvedAt = approvedAt;
    }
} 