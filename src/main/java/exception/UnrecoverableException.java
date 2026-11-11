package exception;

public abstract class UnrecoverableException extends TraceFinderException {

    protected UnrecoverableException(String message) {
        super(message);
    }

    protected UnrecoverableException(String message, Throwable cause) {
        super(message, cause);
    }
}