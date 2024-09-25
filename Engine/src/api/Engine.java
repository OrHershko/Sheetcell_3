package api;


import impl.Range;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Engine {
    void loadFile(String filePath) throws IOException;
    DTO getSheetDTO();
    boolean isCellInBounds(int row, int col);
    void updateCellValue(String cellIdentity, CellValue value, String originalValue);
    DTO getCellDTO(String cellIdentity);
    void checkForLoadedFile();
    Map<Integer,DTO> getSheetsPreviousVersionsDTO();
    void saveSheetToFile(String filePath) throws IOException;
    void loadPreviousSheetFromFile(String filePath) throws IOException, ClassNotFoundException;
    void setNewRowsWidth(int width);
    void setNewColsWidth(int width);
    void addNewRange(String topLeftCell, String bottomRightCell, String rangeName);
    DTO getRangeDTOFromSheet(String rangeName);
    void deleteRangeFromSheet(String rangeName);
    DTO getSortedSheetDTO(List<String> columnToSortBy, String topLeft, String bottomRight);
    Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight);
    DTO getFilteredSheetDTO(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight);
    int getNumOfColumnsInCurrSheet();
    DTO DynamicCalculationOnSheet(String selectedCellId, CellValue newCellValue, String orgValue);
}
