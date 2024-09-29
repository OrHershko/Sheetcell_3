package api;

import impl.sheet.SheetData;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Engine {

    void loadFile(InputStream inputStream, String username) throws IOException;

    DTO getSheetDTO(SheetData sheetData);

    boolean isCellInBounds(int row, int col, SheetData sheetData);

//    void updateCellValue(String cellIdentity, CellValue value, String originalValue);

    void updateCellValue(String cellIdentity, CellValue value, String originalValue, SheetData sheetData);

    DTO getCellDTO(String cellIdentity, SheetData sheetData);

    Map<Integer, DTO> getSheetsPreviousVersionsDTO(SheetData sheetData);

   /* void saveSheetToFile(String filePath) throws IOException;

    void loadPreviousSheetFromFile(String filePath) throws IOException, ClassNotFoundException;

    void setNewRowsWidth(int width);

    void setNewColsWidth(int width);
    */
    void addNewRange(String topLeftCell, String bottomRightCell, String rangeName, SheetData sheetData);

    DTO getRangeDTOFromSheet(String rangeName, SheetData sheetData);

    void deleteRangeFromSheet(String rangeName, SheetData sheetData);

    DTO getSortedSheetDTO(List<String> columnToSortBy, String topLeft, String bottomRight, SheetData sheetData);

    Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight, SheetData sheetData);

    DTO getFilteredSheetDTO(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight, SheetData sheetData);

    int getNumOfColumnsInCurrSheet(SheetData sheetData);

    DTO DynamicCalculationOnSheet(String selectedCellId, CellValue newCellValue, String orgValue, SheetData sheetData);

    List<SheetData> getAllSheetsData(String usernameOfRequester);

    SheetData getSheetData(String sheetName);

}


