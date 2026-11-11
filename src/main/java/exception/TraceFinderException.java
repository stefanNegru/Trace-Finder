package exception;

public abstract class TraceFinderException extends RuntimeException {

    protected TraceFinderException(String message) {
        super(message);
    }

    protected TraceFinderException(String message, Throwable cause) {
        super(message, cause);
    }
}