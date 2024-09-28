package impl.sheet;

import java.util.ArrayList;
import java.util.List;

public class SheetData {
    private String username;
    private String sheetName;
    private String sheetSize;
    private String permissionType;
    private List<PermissionData> permissionsData = new ArrayList<>();

    public SheetData(String username, String sheetName, String sheetSize, String permissionType) {
        this.username = username;
        this.sheetName = sheetName;
        this.sheetSize = sheetSize;
        this.permissionType = permissionType;
        permissionsData.add(new PermissionData("user1", "read", "active"));
    }

    public String getUsername() {
        return username;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getSheetSize() {
        return sheetSize;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public List<PermissionData> getPermissionData() { return permissionsData; }
}
