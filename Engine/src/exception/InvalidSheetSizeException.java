package exception;

public class InvalidSheetSizeException extends RuntimeException {
    public InvalidSheetSizeException(String message) {
        super(message);
    }
}