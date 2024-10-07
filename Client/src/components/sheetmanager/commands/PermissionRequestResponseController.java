package components.sheetmanager.commands;

import components.sheetmanager.SheetManagerController;
import impl.sheet.PermissionData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

import static utils.Constants.APPROVE_PERMISSION;
import static utils.Constants.REJECT_PERMISSION;

public class PermissionRequestResponseController {

    @FXML
    private TableView<PermissionData> permissionTableView;

    @FXML
    private TableColumn<PermissionData, String> usernameColumn;

    @FXML
    private TableColumn<PermissionData, String> sheetNameColumn;

    @FXML
    private TableColumn<PermissionData, String> permissionRequestedColumn;

    @FXML
    private TableColumn<PermissionData, CheckBox> approveColumn;

    @FXML
    private TableColumn<PermissionData, CheckBox> rejectColumn;

    @FXML
    private Button submitButton;

    private SheetManagerController sheetManagerController;

    public void setSheetManagerController(SheetManagerController sheetManagerController) {
        this.sheetManagerController = sheetManagerController;
    }

    private final ObservableList<PermissionData> permissionDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure columns to match PermissionData fields
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        permissionRequestedColumn.setCellValueFactory(new PropertyValueFactory<>("permissionType"));

        setupCheckBoxColumn(approveColumn, true);
        setupCheckBoxColumn(rejectColumn, false);

        permissionTableView.setSelectionModel(null);
        permissionTableView.setItems(permissionDataList);
    }

    private void setupCheckBoxColumn(TableColumn<PermissionData, CheckBox> column, boolean isApproveColumn) {
        column.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PermissionData permissionData = getTableView().getItems().get(getIndex());

                    // הגדרת מצב ראשוני של CheckBox
                    checkBox.setSelected(isApproveColumn ? permissionData.isApproved() : permissionData.isRejected());

                    // Listener לשינוי במצב ה-CheckBox
                    checkBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                        if (isNowSelected) {
                            if (isApproveColumn) {
                                permissionData.setApproved(true);
                                permissionData.setRejected(false); // בטל את הסימון בעמודת Reject
                            } else {
                                permissionData.setRejected(true);
                                permissionData.setApproved(false); // בטל את הסימון בעמודת Approve
                            }
                            permissionTableView.refresh();
                        } else {
                            if (isApproveColumn) {
                                permissionData.setApproved(false);
                            } else {
                                permissionData.setRejected(false);
                            }
                        }
                    });
                    setGraphic(checkBox);
                }
            }
        });
    }



    public void loadPermissionRequests() {
        permissionDataList.addAll(sheetManagerController.getPermissionRequests()
                .stream()
                .filter(permission -> permission.getPermissionStatus().equals("PENDING")).toList());
    }

    @FXML
    private void submitButtonOnClick() throws IOException {
        for (PermissionData permission : permissionDataList) {
            if (permission.isApproved()) {
                sheetManagerController.handlePermissionRequest(permission, APPROVE_PERMISSION);
            } else if (permission.isRejected()) {
                sheetManagerController.handlePermissionRequest(permission, REJECT_PERMISSION);
            }
        }

        submitButton.getScene().getWindow().hide();
    }
}
