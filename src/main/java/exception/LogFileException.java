package exception;

public class LogFileException extends UnrecoverableException {

    public LogFileException(String message) {
        super(message);
    }

    public LogFileException(String message, Throwable cause) {
        super(message, cause);
    }
}