package impl.cell.value;

import api.CellValue;
import exception.BooleanException;
import exception.RangeDoesntExistException;
import exception.WrongParenthesesOrderException;
import impl.EngineImpl;
import impl.Range;
import impl.cell.Cell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class FunctionValue implements CellValue {
    private final FunctionType functionType;
    private final List<CellValue> arguments = new ArrayList<>();
    private Object effectiveValue;
    private Cell activatingCell;

    public FunctionValue(String functionDefinition) {
        List<String> argsStr = extractArguments(functionDefinition);
        functionType = parseFunctionType(argsStr.getFirst().toUpperCase());
        for (String argument : argsStr.subList(1, argsStr.size())) {
            CellValue value = EngineImpl.convertStringToCellValue(argument);
            value.setActivatingCell(activatingCell);
            arguments.add(value);
        }
    }

    public FunctionValue(String functionType, List<CellValue> arguments) {
        this.functionType = FunctionType.valueOf(functionType.toUpperCase());
        this.arguments.addAll(arguments);
    }

    public void calculateAndSetEffectiveValue(){
        try{
            effectiveValue = eval();
        }
        catch(ArithmeticException e){
            effectiveValue = "NaN";
        }
        catch (BooleanException e){
            effectiveValue = "UNKNOWN";
        }
    }

    public void setEffectiveValue(Object value) {
        this.effectiveValue = value;
    }

    public void setActivatingCell(Cell cell) {
        this.activatingCell = cell;
        for (CellValue argument : arguments) {
            argument.setActivatingCell(activatingCell);
        }
    }

    public static List<String> extractArguments(String input) {
        List<String> arguments = new ArrayList<>();
        int level = 0;
        int start = 0;
        boolean insideArgument = false;

        for (int i = 0; i < input.length() && level >= 0; i++) {
            char c = input.charAt(i);

            if (c == '{') {
                if (level == 0) {
                    start = i;
                }
                level++;
            }
            else if (c == '}') {
                level--;
                if (level == 0) {
                    if(start != i)
                        arguments.add(input.substring(start, i));
                    insideArgument = false;
                }
                else{
                    insideArgument = true;
                }
            }
            else if (c == ',' && level == 1) {
                if (insideArgument) {
                    arguments.add(input.substring(start, i));
                }
                start = i + 1;
                insideArgument = false;
            } else if (level == 1 && !insideArgument) {
                start = i;
                insideArgument = true;
            }
        }

        if (insideArgument) {
            arguments.add(input.substring(start));
        }

        if(level != 0)
        {
            throw new WrongParenthesesOrderException();
        }

        return arguments;
    }

    @Override
    public Object eval() {
        switch (functionType) {
            case PLUS:
            case MINUS:
            case TIMES:
            case DIVIDE:
            case MOD:
            case POW:
            case PERCENT:
                try {
                    return evalForTwoDoubles();
                }
                catch (ClassCastException e) {
                    throw new ArithmeticException(String.format("Error: One or more arguments are not valid. Ensure that all inputs for this function are numeric, e.g. {%s,4,5}.", functionType.name()));
                }
            case BIGGER:
            case LESS:
                try {
                    return evalForTwoDoubles();
                }
                catch (ClassCastException e) {
                    throw new BooleanException(String.format("Error: One or more arguments are not valid. Ensure that all inputs for this function are numeric, e.g. {%s,4,5}.", functionType.name()));
                }
            case ABS:
                try {
                    return evalForOneDouble();
                }
                catch (ClassCastException e) {
                    throw new ArithmeticException("Error: argument is not valid. Ensure that the input argument is numeric, e.g. {ABS,3}.");
                }
            case CONCAT:
                try {
                    return evalForTwoStrings();
                }
                catch (ClassCastException e) {
                    return "!UNDEFINED!";
                }
            case SUB:
                try {
                    return evalForSub();
                }
                catch (ClassCastException e) {
                    return "!UNDEFINED!";
                }
            case REF:
                try {
                    return evalForRef();
                }
                catch (ClassCastException e) {
                        throw new RuntimeException("Error: argument is not valid. Ensure that the input argument is a cell identity, e.g. {REF,A4}.");
                    }

            case SUM:
            case AVERAGE:
                try {
                    return evalForRanges();
                }
                catch (ClassCastException e) {
                    throw new RuntimeException("Error: argument is not valid. Ensure that the input argument is an existing range.");
                }
                catch (RangeDoesntExistException e){
                    throw new ArithmeticException(e.getMessage());
                }
            case EQUAL:
                return evalForTwoObj();
            case NOT:
                try{
                    return evalForOneBoolean();
                }
                catch (ClassCastException e) {
                    throw new BooleanException("Error: argument is not valid. Ensure that the input argument is a boolean expression, e.g. {NOT,TRUE}.");
                }
            case AND:
            case OR:
                try{
                    return evalForTwoBooleans();
                }
                catch (ClassCastException e) {
                    throw new BooleanException(String.format("Error: One or more arguments are not valid. Ensure that all inputs for this function are boolean, e.g. {%s,TRUE,FALSE}", functionType.name()));
                }
            case IF:
                try{
                    return evalForIf();
                }
                catch (ClassCastException e) {
                    throw new BooleanException("Error: One or more arguments are not valid. Ensure that the first input for this function is boolean, e.g. {TRUE,4,5}");
                }
        }

        return null;
    }

    private Object evalForIf() {
        checkNumOfArguments(3, "3 arguments");
        Boolean exp1 =  (Boolean) arguments.get(0).eval();
        Object exp2 =  arguments.get(1).eval();
        Object exp3 =  arguments.get(2).eval();
        return functionType.apply(exp1, exp2, exp3);
    }

    private Boolean evalForTwoBooleans() {
        checkNumOfArguments(2, "2 arguments");
        Boolean exp1 =  (Boolean) arguments.get(0).eval();
        Boolean exp2 =  (Boolean) arguments.get(1).eval();
        return functionType.apply(exp1, exp2);
    }

    private Boolean evalForOneBoolean() {
        checkNumOfArguments(1, "1 argument");
        Boolean boolVal =  (Boolean) arguments.getFirst().eval();
        return functionType.apply(boolVal);
    }

    private boolean evalForTwoObj() {
        checkNumOfArguments(2, "2 arguments");
        Object obj1 =  arguments.get(0).eval();
        Object obj2 =  arguments.get(1).eval();
        return functionType.apply(obj1, obj2);
    }

    private double evalForRanges() {
        checkNumOfArguments(1, "1 argument");
        String rangeName = (String) arguments.getFirst().eval();
        Range range = activatingCell.getSheet().getRange(rangeName);

        if(range.getCells().contains(activatingCell)) {
            throw new RuntimeException("Error: The cell on which the function was applied is part of the range given in the function.");
        }

        for(Cell cell : range.getCells()) {
            cell.getCellsImInfluencing().add(activatingCell);
            activatingCell.getCellsImDependentOn().add(cell);
        }

        return functionType.apply(range);
    }

    private Object evalForRef() {
        checkNumOfArguments(1, "1 argument");
        String str = (String) arguments.getFirst().eval();
        return functionType.apply(str, activatingCell).eval();
    }

    private String evalForSub() {
        checkNumOfArguments(3, "3 arguments");
        String str = (String) arguments.get(0).eval();
        int idx1 =  ((Double) arguments.get(1).eval()).intValue();
        int idx2 =  ((Double) arguments.get(2).eval()).intValue();
        return functionType.apply(str, idx1, idx2);
    }

    private String evalForTwoStrings() {
        checkNumOfArguments(2, "2 arguments");
        String str1 = (String) arguments.get(0).eval();
        String str2 = (String) arguments.get(1).eval();
        return functionType.apply(str1, str2);
    }

    private double evalForOneDouble() {
        checkNumOfArguments(1, "1 argument");
        double arg = (double) arguments.getFirst().eval();
        return functionType.apply(arg);
    }

    private Object evalForTwoDoubles() {
        checkNumOfArguments(2, "2 arguments");
        double arg1 = (double) arguments.get(0).eval();
        double arg2 = (double) arguments.get(1).eval();
        return functionType.apply(arg1, arg2);
    }

    public enum FunctionType {
        PLUS {
            @Override
            public Double apply(double arg1, double arg2) {
                return arg1 + arg2;
            }
        },
        MINUS {
            @Override
            public Double apply(double arg1, double arg2) {
                return arg1 - arg2;
            }
        },
        TIMES {
            @Override
            public Double apply(double arg1, double arg2) {
                return arg1 * arg2;
            }

        },
        MOD{
            @Override
            public Double apply(double arg1, double arg2) {
                return arg1 % arg2;
            }
        },
        DIVIDE {
            @Override
            public Double apply(double arg1, double arg2) {
                if (arg2 == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return arg1 / arg2;
            }
        },
        POW {
            @Override
            public Double apply(double arg1, double arg2) {
                if(arg1 == 0 && arg2 < 0){
                    throw new ArithmeticException("Division by zero");
                }
                return Math.pow(arg1, arg2);
            }
        },
        ABS{
            @Override
            public double apply(double arg) {
                return Math.abs(arg);
            }
        },
        CONCAT {
            @Override
            public String apply(String str1, String str2) {
                return str1 + str2;
            }
        },
        SUB {
            @Override
            public String apply(String source, int startIndex, int endIndex) {
                if (startIndex < 0 || endIndex >= source.length() || startIndex > endIndex) {
                    return "!UNDEFINED!";
                }
                return source.substring(startIndex, endIndex + 1);
            }
        },
        PERCENT {
            @Override
            public Double apply(double part, double whole) {
                if(part < 0){
                    throw new ArithmeticException("Negative percent");
                }
                return part * whole/100;
            }
        },
        REF {
            @Override
            public CellValue apply(String cellId, Cell activatingCell){

                if(!isStringInCellIdentityFormat(cellId)){
                    throw new IllegalStateException("Error: " + cellId + " is not a cell identity. Ensure that the argument is in the right format (e.g., {REF,A4}).");
                }

                Cell referancedCell = activatingCell.getSheet().getCell(cellId.toUpperCase());

                if(referancedCell == null)
                {
                    throw new NullPointerException("Error: Cell " + cellId + " cannot be referenced. Please ensure the cell is within the sheet boundaries and contains a value.");
                }

                activatingCell.getCellsImDependentOn().add(referancedCell);
                referancedCell.getCellsImInfluencing().add(activatingCell);

                try {
                    activatingCell.getSheet().detectCycleByDFS();
                }
                catch (IllegalStateException e)
                {
                    throw new IllegalStateException(e.getMessage());
                }

                return referancedCell.getEffectiveValue();
            }
        },
        SUM{
            @Override
            public double apply(Range range){
                double sum = 0;
                for(Cell cell: range.getCells()){
                    if(!cell.getEffectiveValue().getValue().toString().equals("NaN"))
                    {
                        if(isDouble(cell.getEffectiveValue().eval())){
                            sum += (double) cell.getEffectiveValue().eval();
                        }
                    }
                }

                return sum;
            }
        },
        AVERAGE{
            @Override
            public double apply(Range range){
                double sum = 0;
                double count = 0;
                for(Cell cell: range.getCells()){
                    if(isDouble(cell.getEffectiveValue().eval())){
                        sum += (double) cell.getEffectiveValue().eval();
                        count++;
                    }
                }
                if(count == 0){
                        throw new RuntimeException("Error: The specified range contains no numbers, which is not allowed. Please provide a valid range with at least one number.");
                }
                return sum/count;
            }
        },
        EQUAL{
            @Override
            public boolean apply (Object obj1, Object obj2){
                return obj1.equals(obj2);
            }
        },
        NOT{
            @Override
            public Boolean apply (Boolean booleanVal){
                return !booleanVal;
            }

        },
        AND{
            @Override
            public Boolean apply(Boolean exp1, Boolean exp2){
                return exp1 && exp2;
            }
        },
        OR{
            @Override
            public Boolean apply(Boolean exp1, Boolean exp2){
                return exp1 || exp2;
            }
        },
        BIGGER{
            @Override
            public Boolean apply(double arg1, double arg2){
                return arg1 >= arg2;
            }

        },
        LESS{
            @Override
            public Boolean apply(double arg1, double arg2){
                return arg1 <= arg2;
            }

        },
        IF{
            @Override
            public Object apply(Boolean condition, Object thenVal, Object elseVal){

                return condition ? thenVal : elseVal;
            }
        };

        public Object apply(Boolean condition, Object thenVal, Object elseVal){
            throw new UnsupportedOperationException("Error: This function does not support boolean operations");
        }

        public Boolean apply(Boolean exp1, Boolean exp2){
            throw new UnsupportedOperationException("Error: This function does not support boolean operations");
        }
        public Object apply(double arg1, double arg2) {
            throw new UnsupportedOperationException("Error: This function does not support numeric operations");
        }
        public Boolean apply(Boolean booleanVal){
            throw new UnsupportedOperationException("Error: This function does not support boolean operations");
        }

        public boolean apply(Object obj1, Object obj2){
            throw new UnsupportedOperationException("Error: This function does not support boolean operations");
        }

        public String apply(String str1, String str2) {
            throw new UnsupportedOperationException("Error: This function does not support string concatenation");
        }

        public String apply(String source, int startIndex, int endIndex) {
            throw new UnsupportedOperationException("Error: This function does not support substring operations");
        }

        public double apply(double arg) {
            throw new UnsupportedOperationException("Error: This function does not support numeric operations");
        }

        public CellValue apply(String cellId, Cell activatingCell) {
            throw new UnsupportedOperationException("Error: This function does not support referring operations");
        }

        public double apply(Range range){
            throw new UnsupportedOperationException("Error: This function does not support range operations");
        }
    }

    public static boolean isDouble(Object obj) {
        if (obj instanceof Double) {
            return true;
        }
        try {
            Double.parseDouble(obj.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private static boolean isStringInCellIdentityFormat(String str){
        if (str == null || str.length() < 2) {
            return false;
        }

        char firstChar = str.charAt(0);
        String restOfString = str.substring(1);

        if (!Character.isLetter(firstChar)) {
            return false;
        }

        try {
            Integer.parseInt(restOfString);
        } catch (NumberFormatException e) {
            return false; // Not a valid number
        }

        return true;
    }

    private void checkNumOfArguments(int numOfArgumentsExp, String numArgsStr) throws IllegalArgumentException {
        if (arguments.size() != numOfArgumentsExp) {
            throw new IllegalArgumentException("Error: Function " + functionType.name() + " expected " + numArgsStr + ", got " + arguments.size() + ".");
        }
    }

    @Override
    public Object getValue() {
        if(effectiveValue instanceof Double num)
        {
            return convertToIntIfWholeNumber(num);
        }

        return effectiveValue;
    }

    public String convertToIntIfWholeNumber(Double value) {
        if (value % 1 == 0) {
            return String.format("%,d", value.longValue());
        } else {
            return String.format("%,.2f", value);
        }
    }

    private FunctionType parseFunctionType(String functionName) {
        try {
            return FunctionType.valueOf(functionName);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException("Error: Invalid function definition: " + functionName);
        }
    }

    @Override
    public Cell getActivatingCell() {
        return activatingCell;
    }

    @Override
    public FunctionValue clone(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);

            out.writeObject(this);
            out.flush();
            out.close();

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);

            return (FunctionValue) in.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Clone failed", e);
        }
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public List<CellValue> getArguments() {
        return arguments;
    }
}
