package exception;

public class MalformedLineException extends RecoverableException {

    private final int lineNumber;
    private final String originalContent;

    public MalformedLineException(int lineNumber, String originalContent) {
        super("Malformed line at line " + lineNumber);

        this.lineNumber = lineNumber;
        this.originalContent = originalContent;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getOriginalContent() {
        return originalContent;
    }
}