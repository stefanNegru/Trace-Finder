package analysis;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.AnalysisResult;
import model.FlaggedEntry;
import model.MalformedLine;
import model.RawLine;
import model.SuspiciousIp;
import model.UnknownPattern;

public class Analyzer {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                    .withResolverStyle(ResolverStyle.STRICT);

    public static AnalysisResult analyze(
        List<RawLine> rawLines,
        Map<String, Integer> rules
    ) {
        Map<String, Integer> activitySummary = new LinkedHashMap<>();
        List<FlaggedEntry> flaggedEntries = new ArrayList<>();
        List<UnknownPattern> unknownPatterns = new ArrayList<>();
        List<MalformedLine> malformedLines = new ArrayList<>();
        List<SuspiciousIp> suspiciousIps = new ArrayList<>();
            

        for (RawLine rawLine : rawLines) {
            String[] fields = rawLine.content().split("\\|", -1);

            if (fields.length != 5) {
                malformedLines.add(
                        new MalformedLine(rawLine.lineNumber(), rawLine.content())
                );
                continue;
            }

            String timestamp = fields[0].trim();
            String level = fields[1].trim();
            String sourceIp = fields[2].trim();
            String target = fields[3].trim();
            String action = fields[4].trim();

            try {
                LocalDateTime.parse(
                        timestamp,
                        TIMESTAMP_FORMATTER
                );
            } catch (DateTimeParseException e) {
                malformedLines.add(new MalformedLine(rawLine.lineNumber(), rawLine.content()));
                continue;
            }

            if (!rules.containsKey(level)) {
                unknownPatterns.add(
                        new UnknownPattern(rawLine.lineNumber(), rawLine.content())
                );
                continue;
            }

            int count = activitySummary.getOrDefault(level, 0);
            activitySummary.put(level, count + 1);
        }

        

        

        return new AnalysisResult(
                activitySummary,
                flaggedEntries,
                suspiciousIps,
                unknownPatterns,
                malformedLines
        );
    }
}
