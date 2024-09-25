package impl;

import api.*;
import exception.CellOutOfBoundsException;
import exception.FileNotXMLException;
import exception.InvalidSheetSizeException;
import exception.RangeUsedInFunctionException;
import generated.STLCell;
import generated.STLSheet;
import impl.cell.Cell;
import impl.cell.value.*;
import impl.sheet.Sheet;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class EngineImpl implements Engine {
    private static Sheet currentSheet;
    private final DTOFactory DTOFactory;
    private final String JAXB_XML_PACKAGE_NAME = "generated";
    private final int MAX_NUM_OF_ROWS = 50;
    private final int MAX_NUM_OF_COLUMNS = 20;

    public EngineImpl(DTOFactory DTOFactory) {
        this.DTOFactory = DTOFactory;
    }

    @Override
    public void loadFile(String filePath) throws IOException {
        STLSheet currentSTLSheet;
        checkIfFilePathValid(filePath);
        try{
            currentSTLSheet = buildSTLSheetFromXML(filePath);
        }
        catch (JAXBException e){
            throw new RuntimeException("Error: The file is not in the correct format.");
        }
        buildSheetFromSTLSheet(currentSTLSheet);
        Sheet.clearPreviousVersions();
    }

    private void checkIfFilePathValid(String filePath) throws FileNotFoundException, FileNotXMLException {
        File file = new File(filePath);
        if(!file.exists()){
            throw new FileNotFoundException("Error: File is not found in the file path " + filePath);
        }
        if(!file.getName().endsWith(".xml")){
            throw new FileNotXMLException();
        }
    }

    private STLSheet buildSTLSheetFromXML(String filePath)throws IOException, JAXBException{
        InputStream inputStream = new FileInputStream(filePath);
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_PACKAGE_NAME);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (STLSheet) unmarshaller.unmarshal(inputStream);
    }


    private void buildSheetFromSTLSheet(STLSheet currentSTLSheet) {
        checkDataValidity(currentSTLSheet);
        currentSheet = new Sheet();
        currentSheet.setName(currentSTLSheet.getName());
        currentSheet.setNumOfCols(currentSTLSheet.getSTLLayout().getColumns());
        currentSheet.setNumOfRows(currentSTLSheet.getSTLLayout().getRows());
        currentSheet.setColWidth(currentSTLSheet.getSTLLayout().getSTLSize().getColumnWidthUnits());
        currentSheet.setRowHeight(currentSTLSheet.getSTLLayout().getSTLSize().getRowsHeightUnits());
        currentSheet.setActiveCells(currentSTLSheet.getSTLCells().getSTLCell());
        currentSheet.setRangesFromFile(currentSTLSheet.getSTLRanges());
    }

    @Override
    public void addNewRange(String topLeftCell, String bottomRightCell, String rangeName) {
        int topLeftCellRow = Cell.getRowFromCellID(topLeftCell);
        int topLeftCellColumn = Cell.getColumnFromCellID(topLeftCell);
        int bottomRightCellRow = Cell.getRowFromCellID(bottomRightCell);
        int bottomRightCellColumn = Cell.getColumnFromCellID(bottomRightCell);
        currentSheet.checkIfRangeInBoundaries(topLeftCellRow, topLeftCellColumn, bottomRightCellRow, bottomRightCellColumn);
        Range newRange = new Range(rangeName, topLeftCell, bottomRightCell, currentSheet);
        currentSheet.addRange(newRange);
    }

    private void checkDataValidity(STLSheet currentSTLSheet) {
        checkSheetSize(currentSTLSheet.getSTLLayout().getRows(), currentSTLSheet.getSTLLayout().getColumns());
        checkCellsWithinBounds(currentSTLSheet);
    }

    private void checkSheetSize(int rows, int columns) {
        if (rows < 1 || rows > MAX_NUM_OF_ROWS || columns < 1 || columns > MAX_NUM_OF_COLUMNS) {
            throw new InvalidSheetSizeException("Error: The sheet size is not valid," +
                    String.format(" make sure that the number of rows is between 1 and %d and the number of columns is between 1 and %d.", MAX_NUM_OF_ROWS, MAX_NUM_OF_COLUMNS));
        }
    }

    private void checkCellsWithinBounds(STLSheet sheet) {
        int rowCount = sheet.getSTLLayout().getRows();
        int columnCount = sheet.getSTLLayout().getColumns();

        List<STLCell> cells = sheet.getSTLCells().getSTLCell();

        for (STLCell cell : cells) {
            int row = cell.getRow();
            String columnLetter = cell.getColumn();

            int column = convertColumnLetterToNumber(columnLetter);

            if (row < 1 || row > rowCount || column < 1 || column > columnCount) {
                throw new CellOutOfBoundsException("Error: A cell is defined outside the sheet boundaries: (" + row + ", " + columnLetter + ").");
            }
        }
    }

    private int convertColumnLetterToNumber(String columnLetter) {
        if (columnLetter == null || columnLetter.length() != 1 || !Character.isLetter(columnLetter.charAt(0))) {
            throw new IllegalArgumentException("Error: Invalid column letter: " + columnLetter);
        }

        return columnLetter.toUpperCase().charAt(0) - 'A' + 1;
    }

    @Override
    public DTO getSheetDTO() {
        checkForLoadedFile();
        return DTOFactory.createSheetDTO(currentSheet);
    }

    @Override
    public void checkForLoadedFile(){
        if(currentSheet == null)
        {
            throw new NullPointerException("Error: You must load a file to the system before performing this action.");
        }
    }

    @Override
    public DTO getCellDTO(String cellIdentity) {
        checkForLoadedFile();
        Cell currentCell = currentSheet.getCell(cellIdentity);
        if(currentCell == null)
            return DTOFactory.createEmptyCellDTO(cellIdentity);
        return DTOFactory.createCellDTO(currentSheet.getCell(cellIdentity));
    }

    @Override
    public boolean isCellInBounds(int row, int col) {
        checkForLoadedFile();
        return(row >= 0 && row < currentSheet.getNumOfRows() && col >= 0 && col < currentSheet.getNumOfCols());
    }

    @Override
    public void updateCellValue(String cellIdentity, CellValue value, String originalValue) {
        checkForLoadedFile();
        Sheet alternativeSheet = currentSheet.clone();
        List<Cell> topologicalOrder = alternativeSheet.sortActiveCellsTopologicallyByDFS();
        alternativeSheet.updateOrCreateCell(cellIdentity, value, originalValue, false);
        Cell updatedCell = alternativeSheet.getCell(cellIdentity);

        if(!topologicalOrder.contains(updatedCell))
            topologicalOrder.addLast(updatedCell);

        alternativeSheet.recalculateByTopologicalOrder(topologicalOrder);
        alternativeSheet.calculateChangedCells(updatedCell);
        Sheet.addToPreviousVersions(currentSheet);
        currentSheet = alternativeSheet;
    }

    public static CellValue convertStringToCellValue(String newValue) {
        CellValue cellValue;
        newValue = newValue.trim();

        // Check for Boolean
        if (newValue.equalsIgnoreCase("TRUE") || newValue.equalsIgnoreCase("FALSE")) {
            cellValue = new BooleanValue(Boolean.parseBoolean(newValue));
        }
        // Check for Numerical
        else if (newValue.matches("-?\\d+(\\.\\d+)?")) {
            try {
                double numericValue = Double.parseDouble(newValue);
                cellValue = new NumericValue(numericValue);
            }
            catch (NumberFormatException e) {
                throw new NumberFormatException("Error: Invalid numeric value.");
            }
        }
        // Check for Function
        else if (newValue.matches("\\{[A-Za-z]+(,([^,]*)?)*\\}")) {
            cellValue = new FunctionValue(newValue);
        }
        // Otherwise, treat as String
        else {
            cellValue = new StringValue(newValue);
        }

        return cellValue;
    }


    @Override
    public Map<Integer, DTO> getSheetsPreviousVersionsDTO() {
        checkForLoadedFile();
        Map<Integer,Sheet> previousVersions = currentSheet.getPreviousVersions();

        if(previousVersions.isEmpty()){
            throw new RuntimeException("There are no previous versions to look back at.");
        }

        Map<Integer, DTO> previousVersionsDTO = new TreeMap<>();

        for(Map.Entry<Integer,Sheet> entry : previousVersions.entrySet()) {
            previousVersionsDTO.put(entry.getKey(), DTOFactory.createSheetDTO(entry.getValue()));
        }

        return previousVersionsDTO;
    }

    private void checkIfFilePathIsDir(String filePath) throws IOException {
        File file = new File(filePath);

        if (file.isDirectory()) {
            throw new IOException("Error: The provided path is a directory, not a valid file path: " + filePath +
                    ". Make sure that the file path contains the file name.");
        }
    }

    @Override
    public void saveSheetToFile(String filePath) throws IOException {

        checkIfFilePathIsDir(filePath);

        if (!filePath.endsWith(".ser")) {
            filePath = filePath + ".ser";
        }
        try( FileOutputStream fileOut = new FileOutputStream(filePath);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)){
            out.writeObject(currentSheet);
        }
        catch (IOException e) {
            throw new IOException("Error: File cannot be saved in: " + filePath);
        }
    }

    @Override
    public void loadPreviousSheetFromFile(String filePath) throws IOException, ClassNotFoundException {

        checkIfFilePathIsDir(filePath);

        if (!filePath.endsWith(".ser")) {
            filePath = filePath + ".ser";
        }
        try (FileInputStream fileIn = new FileInputStream(filePath);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            currentSheet = (Sheet) in.readObject();
        }
        catch (IOException e) {
            throw new FileNotFoundException("Error: File not found in: " + filePath);
        }
        catch (ClassNotFoundException e){
            throw new ClassNotFoundException("Error: File failed to load from: " + filePath + ". Please make sure that the file is in the correct format.");
        }
    }

    @Override
    public void setNewRowsWidth(int width) {
        currentSheet.setRowHeight(width);
    }

    @Override
    public void setNewColsWidth(int width) {
        currentSheet.setColWidth(width);
    }

    @Override
    public DTO getRangeDTOFromSheet(String rangeName) {
        return DTOFactory.createRangeDTO(currentSheet.getRange(rangeName));
    }

    @Override
    public void deleteRangeFromSheet(String rangeName) {
        for(Cell cell : currentSheet.getActiveCells().values()) {
            if(cell.getEffectiveValue() instanceof FunctionValue functionValue) {
                if(functionValue.getFunctionType().equals(FunctionValue.FunctionType.AVERAGE) || functionValue.getFunctionType().equals(FunctionValue.FunctionType.SUM))
                {
                    if(functionValue.getArguments().getFirst().getValue().equals(rangeName))
                    {
                        throw new RangeUsedInFunctionException("The range '" + rangeName + "cannot be deleted because it is used in a function.");
                    }
                }
            }
        }

        currentSheet.deleteRange(rangeName);

    }

    @Override
    public DTO getSortedSheetDTO(List<String> columnsToSortBy, String topLeft, String bottomRight) {

        int topLeftRow = Cell.getRowFromCellID(topLeft);
        int topLeftCol = Cell.getColumnFromCellID(topLeft);
        int bottomRightRow = Cell.getRowFromCellID(bottomRight);
        int bottomRightCol = Cell.getColumnFromCellID(bottomRight);

        try {
            List<Map.Entry<Integer, List<Cell>>> rowsList = createListOfRows(topLeftRow, topLeftCol, bottomRightRow, bottomRightCol);
            sortRowsList(columnsToSortBy, rowsList, topLeftCol);
            Sheet sortedSheet = createModifiedSheet(topLeftRow, topLeftCol, bottomRightRow, bottomRightCol, rowsList);
            return DTOFactory.createSheetDTO(sortedSheet);
        }
        catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Make sure the range provided contains only numeric values.");
        }

    }

    private void sortRowsList(List<String> columnsToSortBy, List<Map.Entry<Integer, List<Cell>>> rowsList, int topLeftCol) {

        for (int i = columnsToSortBy.size() - 1; i >= 0; i--) {
            String columnToSortBy = columnsToSortBy.get(i).replace("Column ", "").trim();
            int colToSortBy = Cell.getColumnFromCellID(columnToSortBy + "1");

            rowsList.sort((entry1, entry2) -> {
                Cell cell1 = entry1.getValue().get(colToSortBy - topLeftCol);
                Cell cell2 = entry2.getValue().get(colToSortBy - topLeftCol);

                try {
                    Double value1 = Double.valueOf((String) cell1.getEffectiveValue().getValue());
                    Double value2 = Double.valueOf((String) cell2.getEffectiveValue().getValue());
                    return value1.compareTo(value2);
                }
                catch (NumberFormatException e) {
                    throw new NumberFormatException("Make sure the range provided contains only numeric values.");
                }
            });
        }
    }

    private Sheet createModifiedSheet(int topLeftRow, int topLeftCol, int bottomRightRow, int bottomRightCol, List<Map.Entry<Integer, List<Cell>>> rowsList) {
        Sheet modifiedSheet = currentSheet.clone();
        clearRangeValues(topLeftRow, topLeftCol, bottomRightRow, bottomRightCol, modifiedSheet);
        updateRangeFromRowsList(topLeftRow, topLeftCol, rowsList, modifiedSheet);
        return modifiedSheet;
    }

    private static void updateRangeFromRowsList(int topLeftRow, int topLeftCol, List<Map.Entry<Integer, List<Cell>>> rowsList, Sheet modifiedSheet) {
        int currentRow = topLeftRow;

        for (Map.Entry<Integer, List<Cell>> entry : rowsList) {
            List<Cell> cellsInRow = entry.getValue();
            int currentCol = topLeftCol;

            for (Cell cell : cellsInRow) {
                String newCellID = Cell.getCellIDFromRowCol(currentRow, currentCol);
                modifiedSheet.updateOrCreateCell(newCellID, cell.getEffectiveValue(), cell.getOriginalValue(), false);

                currentCol++;
            }

            currentRow++;
        }
    }

    private void clearRangeValues(int topLeftRow, int topLeftCol, int bottomRightRow, int bottomRightCol, Sheet modifiedSheet) {
        for (int row = topLeftRow; row <= bottomRightRow; row++) {
            for (int col = topLeftCol; col <= bottomRightCol; col++) {
                String cellId = Cell.getCellIDFromRowCol(row, col);
                Cell cell = modifiedSheet.getActiveCells().get(cellId);
                if (cell != null) {
                    modifiedSheet.updateOrCreateCell(cellId, new StringValue(""), "", false);
                }
            }
        }
    }

    private List<Map.Entry<Integer, List<Cell>>> createListOfRows(int topLeftRow, int topLeftCol, int bottomRightRow, int bottomRightCol) {
        List<Map.Entry<Integer, List<Cell>>> rowsList = new ArrayList<>();

        for (int row = topLeftRow; row <= bottomRightRow; row++) {
            List<Cell> cellsInRow = new ArrayList<>();
            for (int col = topLeftCol; col <= bottomRightCol; col++) {
                String cellID = Cell.getCellIDFromRowCol(row, col);
                Cell cell = currentSheet.getCell(cellID);
                if (cell != null) {
                    cellsInRow.add(cell);
                }
            }
            rowsList.add(Map.entry(row, cellsInRow));
        }

        return rowsList;
    }

    @Override
    public Set<String> getValuesFromColumn(String column, String topLeft, String bottomRight) {

        return currentSheet.getCellsInRange(topLeft, bottomRight)
                .stream()
                .filter(cell -> column.equals(String.valueOf(cell.getIdentity().charAt(0))))
                .map(cell-> cell.getEffectiveValue().getValue().toString())
                .collect(Collectors.toSet());
    }

    private void filterRowsList(Map<String, Set<String>> colToSelectedValues, List<Map.Entry<Integer, List<Cell>>> rowsList) {

        for (Map.Entry<String, Set<String>> colToValuesEntry : colToSelectedValues.entrySet()) {
            // שימוש ב־Iterator כדי להסיר את השורות בצורה בטוחה
            Iterator<Map.Entry<Integer, List<Cell>>> rowIterator = rowsList.iterator();
            while (rowIterator.hasNext()) {
                Map.Entry<Integer, List<Cell>> rowEntry = rowIterator.next();
                boolean isRowRemoved = rowEntry.getValue().stream()
                        .filter(cell -> colToValuesEntry.getKey().equals(String.valueOf(cell.getIdentity().charAt(0))))
                        .noneMatch(cell -> colToValuesEntry.getValue().contains(cell.getEffectiveValue().getValue().toString()));

                // אם השורה לא עומדת בתנאים, נסיר אותה באמצעות ה־Iterator
                if (isRowRemoved) {
                    rowIterator.remove();
                }
            }
        }

    }


    @Override
    public DTO getFilteredSheetDTO(Map<String, Set<String>> colToSelectedValues, String topLeft, String bottomRight) {
        int topLeftRow = Cell.getRowFromCellID(topLeft);
        int topLeftCol = Cell.getColumnFromCellID(topLeft);
        int bottomRightRow = Cell.getRowFromCellID(bottomRight);
        int bottomRightCol = Cell.getColumnFromCellID(bottomRight);

        List<Map.Entry<Integer, List<Cell>>> rowsList = createListOfRows(topLeftRow, topLeftCol, bottomRightRow, bottomRightCol);
        filterRowsList(colToSelectedValues, rowsList);
        Sheet filteredSheet = createModifiedSheet(topLeftRow, topLeftCol, bottomRightRow, bottomRightCol, rowsList);
        return DTOFactory.createSheetDTO(filteredSheet);
    }

    @Override
    public int getNumOfColumnsInCurrSheet() {
        return currentSheet.getNumOfCols();
    }

    @Override
    public DTO DynamicCalculationOnSheet(String cellIdentity, CellValue value, String originalValue) {
        Sheet alternativeSheet = currentSheet.clone();
        List<Cell> topologicalOrder = alternativeSheet.sortActiveCellsTopologicallyByDFS();
        alternativeSheet.updateOrCreateCell(cellIdentity, value, originalValue, false);
        Cell updatedCell = alternativeSheet.getCell(cellIdentity);

        if(!topologicalOrder.contains(updatedCell))
            topologicalOrder.addLast(updatedCell);

        alternativeSheet.recalculateByTopologicalOrder(topologicalOrder);
        alternativeSheet.calculateChangedCells(updatedCell);

        return DTOFactory.createSheetDTO(alternativeSheet);
    }
}
