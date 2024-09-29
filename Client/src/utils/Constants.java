package utils;

/*
import com.google.gson.Gson;
*/

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

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/Server_Web_exploded";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String UPLOAD_FILE = FULL_SERVER_PATH + "/uploadFile";
    public final static String GET_ALL_SHEETS_DATA = FULL_SERVER_PATH + "/getAllSheetsData";
    public final static String GET_SHEET_DATA = FULL_SERVER_PATH + "/getSheetData";
    public final static String UPDATE_CELL = FULL_SERVER_PATH + "/updateCell";
    public final static String GET_SHEET_DTO = FULL_SERVER_PATH + "/getSheetDTO";
    public final static String SET_CURRENT_SHEET = FULL_SERVER_PATH + "/setCurrentSheet";

    /*// GSON instance
    public final static Gson GSON_INSTANCE = new Gson();*/
}
