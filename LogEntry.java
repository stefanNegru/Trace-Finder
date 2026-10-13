import java.time.LocalDateTime;

public class LogEntry {
    private final LocalDateTime timestamp;
    private final String level;
    private final String sourceIp;
    private final String target;
    private final String action;
    private final int lineNumber;
    private final String rawLine;

    public LogEntry(
            LocalDateTime timestamp, 
            String level, 
            String sourceIp, 
            String target, 
            String action, 
            int lineNumber, 
            String rawLine) {
        this.timestamp = timestamp;
        this.level = level;
        this.sourceIp = sourceIp;
        this.target = target;
        this.action = action;
        this.lineNumber = lineNumber;
        this.rawLine = rawLine;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getTarget() {
        return target;
    }

    public String getAction() {
        return action;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getRawLine() {
        return rawLine;
    }
}
