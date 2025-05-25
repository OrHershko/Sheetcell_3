package exception;

public class FileNotXMLException extends RuntimeException {

    public FileNotXMLException() {
        super("Error: The provided file is not in XML format. Please make sure you file ends with \".xml\"");
    }

    public FileNotXMLException(String message) {
        super(message);
    }
}
