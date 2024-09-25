package exception;

public class RangeDoesntExistException extends RuntimeException {
    public RangeDoesntExistException(String message) {
        super(message);
    }
}
