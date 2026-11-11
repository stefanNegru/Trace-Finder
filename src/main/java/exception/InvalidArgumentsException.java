package exception;

public class InvalidArgumentsException extends UnrecoverableException {

    public InvalidArgumentsException(String message) {
        super(message);
    }

    public InvalidArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }
}