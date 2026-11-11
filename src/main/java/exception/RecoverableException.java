package exception;

public abstract class RecoverableException extends TraceFinderException {

    protected RecoverableException(String message) {
        super(message);
    }

    protected RecoverableException(String message, Throwable cause) {
        super(message, cause);
    }
}