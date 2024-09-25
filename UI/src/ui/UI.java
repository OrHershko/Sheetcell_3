package ui;

import api.CellValue;
import api.DTO;
import api.Engine;
import dto.CellDTO;
import dto.DTOFactoryImpl;
import dto.SheetDTO;
import exception.CellOutOfBoundsException;
import exception.FileNotXMLException;
import impl.*;
import utility.CellCoord;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UI {
    Engine engine = new EngineImpl(new DTOFactoryImpl());
    Boolean isProgramRunning = true;

    public enum MenuOptions {
        LOAD_XML_FILE(1, "Load XML File"), DISPLAY_SHEET(2, "Display Sheet"), DISPLAY_CELL(3, "Display Cell"), UPDATE_CELL(4,"Update Cell"), DISPLAY_VERSIONS(5,"Display Previous Versions"),SAVE_CURR_SHEET(6,"Save Sheet to File"),LOAD_PREVIOUS_SHEET(7,"Load Previously Saved Sheet"), EXIT(8,"Exit");

        private final int value;
        private final String name;

        MenuOptions(int value, String name) {
            this.value = value;
            this.name = name;
        }
        public int getValue() {
            return value;
        }
        public String getName() {
            return name;
        }

    }

    public void Run(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("""
Welcome to the Sheetcell!
*************************""");
        while(isProgramRunning) {
            DisplayMenu(scanner);
        }
    }
    public void DisplayMenu(Scanner scanner) {
        PrintMenu();
        try {
            int userInput;
            try {
                userInput = scanner.nextInt();
            }
            catch (InputMismatchException e) {
                throw new InputMismatchException(String.format("Error: The input provided is not in the correct format. Please enter a number between 1 and %d.", MenuOptions.values().length));
            }
            if (userInput >= 1 && userInput <= MenuOptions.values().length) {

                MenuOptions option = MenuOptions.values()[userInput-1];

                switch (option) {
                    case LOAD_XML_FILE:
                        loadFile();
                        break;
                    case DISPLAY_SHEET:
                        printSheet();
                        break;
                    case DISPLAY_CELL:
                        printCell();
                        break;
                    case UPDATE_CELL:
                        updateCell();
                        break;
                    case DISPLAY_VERSIONS:
                        displayPreviousVersions();
                        break;
                    case SAVE_CURR_SHEET:
                        saveCurrSheetToFile();
                        break;
                    case LOAD_PREVIOUS_SHEET:
                        loadPreviousSheetFromFile();
                        break;
                    case EXIT:
                        isProgramRunning = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            }
            else {
                System.out.println(String.format("Error: Option %d does not exist. Please enter a number between 1 and %d.",userInput,MenuOptions.values().length));
            }
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            scanner.nextLine();
        }
    }

    private void loadPreviousSheetFromFile() throws IOException, ClassNotFoundException {
        System.out.println("Please enter a file path for loading the sheet (e.g., C:\\Users\\YourName\\Documents\\mySheet):");
        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.nextLine();
        engine.loadPreviousSheetFromFile(filePath);
        System.out.println("Sheet loaded from file successfully.");
    }

    private void saveCurrSheetToFile() throws IOException {
        engine.checkForLoadedFile();
        System.out.println("Please enter the complete file path including the file name to save the sheet (e.g., C:\\Users\\YourName\\Documents\\mySheet) :");
        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.nextLine();
        engine.saveSheetToFile(filePath);
        System.out.println("Sheet saved to file successfully.");
    }

    private void displayPreviousVersions() {
        Map<Integer, DTO> sheetsPreviousVersionsDTO = engine.getSheetsPreviousVersionsDTO();
        printPreviousVersionsTable(sheetsPreviousVersionsDTO);
        peakOnPreviousVersion(sheetsPreviousVersionsDTO);
    }

    private void peakOnPreviousVersion(Map<Integer, DTO> sheetsPreviousVersionsDTO) {
        System.out.println("Please enter a previous version number to look back at:");
        int userInput;

        try{
            Scanner scanner = new Scanner(System.in);
            userInput = scanner.nextInt();
            if(sheetsPreviousVersionsDTO.containsKey(userInput)) {
                System.out.println(convertSheetDTOToString((SheetDTO) sheetsPreviousVersionsDTO.get(userInput)));
            }
            else{
                System.out.print(String.format("Error: Previous version %s does not exist, ",userInput));

                if(sheetsPreviousVersionsDTO.size()==1){
                    System.out.println("only version 1 is currently available for viewing. Enter 1 to look back at this version.");
                }
                else {
                    System.out.println(String.format("please enter a number between 1 and %d.",sheetsPreviousVersionsDTO.size()));
                }
            }
        }
        catch (InputMismatchException e){
            System.out.println("Error: The input provided is not in the correct format. Please enter a positive whole number.");
        }
    }

    void printPreviousVersionsTable(Map<Integer, DTO> sheetsPreviousVersionsDTO){

        StringBuilder sb = new StringBuilder();
        sb.append("""
                Version    Changed Cells Count
                *******    *******************
                """);

        for(Map.Entry<Integer, DTO> entry : sheetsPreviousVersionsDTO.entrySet()) {
            int version = entry.getKey();
            SheetDTO currSheet = (SheetDTO)entry.getValue();
            sb.append(String.format("%-10d %-19d%n", version, currSheet.getChangedCellsCount()));
        }

        System.out.println(sb);
    }

    private void loadFile() {
        System.out.println("Please enter a file path to load:");
        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.nextLine();
        try {
            engine.loadFile(filePath);
            System.out.println("File loaded successfully.");
        }
        catch(FileNotFoundException | FileNotXMLException e){
            System.out.println(e.getMessage());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    private void updateCell() {
        engine.checkForLoadedFile();
        CellCoord cellInput = getCheckAndPrintBasicCellInfo("update its value:");
        System.out.println("\nPlease enter a new value for the cell:");
        Scanner scanner = new Scanner(System.in);
        String orgValue = scanner.nextLine();
        CellValue newCellValue = EngineImpl.convertStringToCellValue(orgValue);
        engine.updateCellValue(cellInput.getIdentity(), newCellValue, orgValue);
        printSheet();
    }

    private void printCell() {
        engine.checkForLoadedFile();
        CellCoord cellInput =  getCheckAndPrintBasicCellInfo("view its value and status:");
        CellDTO currCellDTO = (CellDTO) engine.getCellDTO(cellInput.getIdentity());
        int currVersion = currCellDTO.getVersion();
        System.out.print("Current version: ");

        if (currVersion != 0) {
            System.out.println(currVersion);
        }
        else{
            System.out.println();
        }
        String lst1 = String.join(", ", currCellDTO.getCellsImDependentOn());
        System.out.println("Cells Dependency List: " + lst1);
        String lst2 = String.join(", ", currCellDTO.getCellsImInfluencing());
        System.out.println("Cells Influence List: " + lst2);
    }

    private CellCoord getCheckAndPrintBasicCellInfo(String massage){
        CellCoord cellInput = getAndCheckCellInput(massage);
        try {
            String cellDataToPrint = convertCellDTOToString(cellInput.getIdentity());
            System.out.println(cellDataToPrint);
        }
        catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return cellInput;
    }

    private String convertCellDTOToString(String cellIdentity) {
        StringBuilder sb = new StringBuilder();
        CellDTO cellDTO = (CellDTO) engine.getCellDTO(cellIdentity);
        sb.append("Cell Identity: ").append(cellIdentity).append("\n");
        sb.append("Effective Value: ").append(cellDTO.getEffectiveValue().getValue()).append("\n");
        sb.append("Original Value: ").append(cellDTO.getOriginalValue());
        return sb.toString();
    }

    private CellCoord getAndCheckCellInput(String massage) {
        Pattern cellPattern = Pattern.compile("^[A-Z]+[1-9][0-9]*$");
        int col;
        int row;
        String cellIdentity;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter the cell identity (e.g., A4) to " + massage);
        cellIdentity = scanner.nextLine().trim().toUpperCase();

        if (cellPattern.matcher(cellIdentity).matches()) {
            String columnString = cellIdentity.replaceAll("[0-9]", "");
            String rowString = cellIdentity.replaceAll("[A-Z]", "");
            col = extractColumn(columnString);
            row = extractRow(rowString);

            if (!engine.isCellInBounds(row, col)) {
                throw new CellOutOfBoundsException("Invalid cell identity, Please enter a cell within the sheet boundaries.");
            }
        }
        else {
            throw new InputMismatchException("Invalid cell identity, Please enter a cell in the right format (e.g., A4).");
        }

        return new CellCoord(row, col, cellIdentity);
    }

    private static int extractRow(String cellName) {
        String rowPart = cellName.replaceAll("[A-Z]+", "");
        return Integer.parseInt(rowPart) - 1;
    }

    private static int extractColumn(String cellName) {
        String columnPart = cellName.replaceAll("[0-9]+", "");
        int column = 0;

        for (int i = 0; i < columnPart.length(); i++) {
            column = column * 26 + (columnPart.charAt(i) - 'A' + 1);
        }

        return column - 1;
    }


    private void PrintMenu() {
        System.out.println("""

Please select an option by entering its corresponding number from the menu below:""");
        for (MenuOptions menuOption : MenuOptions.values()){
            System.out.println(menuOption.getValue() + ". " + menuOption.getName());
        }
    }

    private void printSheet() {
        String SheetDataToPrint = convertSheetDTOToString((SheetDTO) engine.getSheetDTO());
        System.out.println(SheetDataToPrint);
    }

    private String convertSheetDTOToString(SheetDTO sheetDTO) {
        int NUM_OF_SPACES = 3;
        StringBuilder sb = new StringBuilder();
        int rowsCounter = 1;
        char colCounter = 'A';
        int widthOfFirstCol = countDigits(sheetDTO.getNumOfRows());

        sb.append("Name: ").append(sheetDTO.getName()).append("\n");
        sb.append("Version: ").append(sheetDTO.getVersion()).append("\n");
        sb.append(String.format("%" + (widthOfFirstCol+NUM_OF_SPACES) + "s", ""));

        for(int i=0;i<sheetDTO.getNumOfCols();i++){
            sb.append(String.format("%-" + (sheetDTO.getColWidth()+1) + "s", colCounter++));
        }
        sb.append("\n");

        for (int i = 0 ; i < sheetDTO.getNumOfRows(); i++) {
            sb.append(String.format("%02d", rowsCounter++)).append(" ");
            for (int j = 0; j < sheetDTO.getNumOfCols(); j++) {

                String cellIdentity = convertRowAndColToString(i,j);
                String cellValue = createCellValueToPrint(cellIdentity,sheetDTO);

                if (cellValue.length() > sheetDTO.getColWidth()) {
                    cellValue = cellValue.substring(0, sheetDTO.getColWidth());
                }
                sb.append(String.format("|" + "%-" + sheetDTO.getColWidth() + "s", cellValue));
            }
            sb.append("|\n");
            for(int j = 0; j < sheetDTO.getRowHeight()-1 ; j++){
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String createCellValueToPrint(String cellIdentity, SheetDTO sheetDTO) {
        String cellValue = " ";
        if(sheetDTO.getActiveCells().get(cellIdentity) != null){
            cellValue = sheetDTO.getActiveCells().get(cellIdentity).getEffectiveValue().getValue().toString();
        }
        return cellValue;
    }


    private String convertRowAndColToString(int row, int col) {
        char newCol = (char) ('A' + col);
        int newRow = row + 1;

        return String.valueOf(newCol) + newRow;
    }

    public static int countDigits(int number) {
        if (number == 0) {
            return 1;
        }

        int count = 0;
        while (number != 0) {
            number /= 10;
            count++;
        }
        return count;
    }

}
