package analysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.AnalysisResult;
import model.FlaggedEntry;
import model.LogEntry;
import model.MalformedLine;
import model.SuspiciousIp;
import model.UnknownPattern;

public class Analyzer {

    public static AnalysisResult analyze(
            List<LogEntry> entries,
            Map<String, Integer> rules
    ) {

        Map<String, Integer> activitySummary = new HashMap<>();

        for (LogEntry entry : entries) {
            String level = entry.getLevel();

            int count = activitySummary.getOrDefault(level, 0);
            activitySummary.put(level, count + 1);
        }
        

        List<FlaggedEntry> flaggedEntries = new ArrayList<>();
        List<UnknownPattern> unknownPatterns = new ArrayList<>();
        List<MalformedLine> malformedLines = new ArrayList<>();
        List<SuspiciousIp> suspiciousIps = new ArrayList<>();

        return new AnalysisResult(
                activitySummary,
                flaggedEntries,
                suspiciousIps,
                unknownPatterns,
                malformedLines
        );
    }
}
