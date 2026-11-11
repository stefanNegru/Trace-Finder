package exception;

public class RuleFileException extends UnrecoverableException {

    public RuleFileException(String message) {
        super(message);
    }

    public RuleFileException(String message, Throwable cause) {
        super(message, cause);
    }
}