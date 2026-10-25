package model;
import java.util.List;
import java.util.Map;

public record AnalysisResult(
        Map<String, Integer> activitySummary,
        List<FlaggedEntry> flaggedEntries,
        List<SuspiciousIp> suspiciousIps,
        List<UnknownPattern> unknownPatterns,
        List<MalformedLine> malformedLines
) {
}