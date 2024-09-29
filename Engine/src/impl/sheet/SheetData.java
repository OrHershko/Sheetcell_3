package impl.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SheetData {
    private String username;
    private String sheetName;
    private String sheetSize;
    private List<PermissionData> permissionsData = new ArrayList<>();

    public SheetData(String username, String sheetName, String sheetSize) {
        this.username = username;
        this.sheetName = sheetName;
        this.sheetSize = sheetSize;
        permissionsData.add(new PermissionData(username, "OWNER", "APPROVED"));
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

    public void addPermissionData(PermissionData permissionData) {
        permissionsData.add(permissionData);
    }

    public List<PermissionData> getPermissionData() { return permissionsData; }

    public String getPermissionTypeForUser(String usernameOfRequester) {
        for (PermissionData permissionData : permissionsData) {
            if (permissionData.getUsername().equals(usernameOfRequester)) {
                return permissionData.getPermissionType();
            }
        }
        return "NO PERMISSION";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SheetData sheetData = (SheetData) o;
        return Objects.equals(username, sheetData.username) &&
                Objects.equals(sheetName, sheetData.sheetName) &&
                Objects.equals(sheetSize, sheetData.sheetSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, sheetName, sheetSize);
    }
}
