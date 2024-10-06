package utils;

/*
import com.google.gson.Gson;
*/

import api.CellValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String JHON_DOE = "<Anonymous>";
    public final static int REFRESH_RATE = 2000;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/main/App.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/components/login/Login.fxml";
    public final static String SHEET_MANAGER_FXML_RESOURCE_LOCATION = "/components/sheetmanager/SheetManager.fxml";
    public final static String REQUEST_PERMISSION_FXML_RESOURCE_LOCATION = "/components/sheetmanager/commands/RequestPermissionPopUp.fxml";

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/Server_Web_exploded";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String UPLOAD_FILE = FULL_SERVER_PATH + "/uploadFile";
    public final static String GET_ALL_SHEETS_DATA = FULL_SERVER_PATH + "/getAllSheetsData";
    public final static String UPDATE_CELL = FULL_SERVER_PATH + "/updateCell";
    public final static String GET_SHEET_DTO = FULL_SERVER_PATH + "/getSheetDTO";
    public final static String GET_CELL_DTO = FULL_SERVER_PATH + "/getCellDTO";
    public final static String ADD_NEW_RANGE = FULL_SERVER_PATH + "/addNewRange";
    public final static String GET_RANGE_DTO = FULL_SERVER_PATH + "/getRangeDTO";
    public final static String ADD_PERMISSION = FULL_SERVER_PATH + "/addPermissionToSelectedSheet";
    public final static String GET_SHEET_VERSIONS_ENDPOINT = FULL_SERVER_PATH + "/getSheetsPreviousVersions";
    public final static String DELETE_RANGE_FROM_SHEET = FULL_SERVER_PATH + "/deleteRangeFromSheet";
    public final static String CHECK_RANGE_OF_CELLS = FULL_SERVER_PATH + "/checkRangeOfCells";
    public final static String SORT_SHEET_BY_COLUMNS = FULL_SERVER_PATH + "/sortSheetByColumns";
    public final static String GET_VALUES_FROM_COLUMN = FULL_SERVER_PATH + "/getValuesFromColumn";
    public final static String FILTER_SHEET_URL = FULL_SERVER_PATH + "/filterSheet";
    public final static String GET_NUM_OF_COLUMNS_IN_GRID_URL = FULL_SERVER_PATH + "/getNumOfColumnsInGrid";
    public final static String DYNAMIC_CALCULATION_URL = FULL_SERVER_PATH + "/dynamicCalculation";

    // GSON instance
    public final static Gson GSON_INSTANCE = new GsonBuilder()
            .registerTypeAdapter(CellValue.class, new CellValueAdapter())
            .create();;
}
