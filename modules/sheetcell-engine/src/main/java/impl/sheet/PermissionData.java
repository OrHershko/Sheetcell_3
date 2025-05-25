package impl.sheet;

public class PermissionData {
    private String username;
    private String permissionType;
    private String permissionStatus;
    private String sheetName;
    private boolean approved = false;
    private boolean rejected = false;

    public PermissionData(String username, String permissionType, String permissionStatus, String sheetName) {
        this.username = username;
        this.permissionType = permissionType;
        this.permissionStatus = permissionStatus;
        this.sheetName = sheetName;
    }

    public String getUsername() {
        return username;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public String getPermissionStatus() {
        return permissionStatus;
    }

    public String getSheetName() { return sheetName; }

    public boolean isApproved() { return approved; }

    public void setApproved(boolean approved) { this.approved = approved; }

    public boolean isRejected() { return rejected; }

    public void setRejected(boolean rejected) { this.rejected = rejected; }

    public void setPermissionStatus(String permissionStatus) {
        this.permissionStatus = permissionStatus;
    }
}
