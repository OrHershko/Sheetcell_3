package exception;

public class WrongParenthesesOrderException extends RuntimeException {
    public WrongParenthesesOrderException(){
        super("Error: wrong parentheses sequence detected. Please check the order and balance of your parentheses.");
    }
}
