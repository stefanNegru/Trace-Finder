package analysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.AnalysisResult;
import model.FlaggedEntry;
import model.MalformedLine;
import model.RawLine;
import model.SuspiciousIp;
import model.UnknownPattern;

public class Analyzer {

    public static AnalysisResult analyze(
            List<RawLine> rawLines,
            Map<String, Integer> rules
        ) {
            Map<String, Integer> activitySummary = new HashMap<>();
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

            String level = fields[1].trim();

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
