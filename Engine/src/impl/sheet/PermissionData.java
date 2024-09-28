package impl.sheet;

public class PermissionData {
    private String username;
    private String permissionType;
    private String permissionStatus;

    public PermissionData(String username, String permissionType, String permissionStatus) {
        this.username = username;
        this.permissionType = permissionType;
        this.permissionStatus = permissionStatus;
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
}
