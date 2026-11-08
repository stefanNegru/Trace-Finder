package analysis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import model.AnalysisResult;
import model.RawLine;

class AnalyzerTest {
    @Test
    void countsKnownLevel() {
        
        List<RawLine> rawLines = List.of(
            new RawLine(1, "2024-03-15 02:14:08 | INFO | 192.168.1.45 | /login | success")
        );
        Map<String, Integer> rules = Map.of("INFO", 1);
        AnalysisResult result = Analyzer.analyze(rawLines, rules, null, null);

        assertEquals(1, result.activitySummary().get("INFO"));
    }

    @Test
    void includesEntryAtStartTime() {

        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 02:00:00 | INFO | 192.168.1.45 | /login | success")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(1, result.activitySummary().get("INFO"));
    }

    @Test
    void excludesEntryAtEndTime() {

        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 03:00:00 | INFO | 192.168.1.45 | /login | success")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(0, result.activitySummary().get("INFO"));
    }

    @Test
    void excludesEntriesOutsideWindow() {

        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 01:59:59 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(2,
                "2024-03-15 02:30:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(3,
                "2024-03-15 03:00:01 | INFO | 192.168.1.45 | /login | success")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(1, result.activitySummary().get("INFO"));
    }

    @Test
    void returnsEmptyResultsForEmptyWindow() {

        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 01:00:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(2,
                "2024-03-15 04:00:00 | ERROR | 192.168.1.46 | /admin | failed")
        );

        Map<String, Integer> rules = Map.of(
            "INFO", 1,
            "ERROR", 3
        );

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(0, result.activitySummary().get("INFO"));
        assertEquals(0, result.activitySummary().get("ERROR"));
        assertEquals(0, result.flaggedEntries().size());
        assertEquals(0, result.suspiciousIps().size());
        assertEquals(0, result.unknownPatterns().size());
    }

    @Test
    void acceptsEqualStartAndEndTime() {

        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 02:00:00 | INFO | 192.168.1.45 | /login | success")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(0, result.activitySummary().get("INFO"));
        assertEquals(0, result.flaggedEntries().size());
        assertEquals(0, result.suspiciousIps().size());
        assertEquals(0, result.unknownPatterns().size());
    }

    @Test
    void preservesMalformedLinesOutsideWindow() {

        List<RawLine> rawLines = List.of(
            new RawLine(1, "invalid line"),
            new RawLine(2,
                "2024-03-15 02:30:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(3, "another invalid line")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(2, result.malformedLines().size());

        assertEquals(1, result.malformedLines().get(0).lineNumber());
        assertEquals("invalid line",
                result.malformedLines().get(0).content());

        assertEquals(3, result.malformedLines().get(1).lineNumber());
        assertEquals("another invalid line",
                result.malformedLines().get(1).content());
    }

    @Test
    void preservesOriginalLineNumbers() {

        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 01:00:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(2,
                "2024-03-15 02:30:00 | UNKNOWN | 192.168.1.46 | /admin | failed"),
            new RawLine(3,
                "2024-03-15 04:00:00 | INFO | 192.168.1.47 | /logout | success")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(1, result.unknownPatterns().size());
        assertEquals(2, result.unknownPatterns().get(0).lineNumber());
    }

    @Test
    void ignoresUnknownPatternsOutsideWindow() {
        List<RawLine> rawLines = List.of(
            new RawLine(1, "2024-03-15 01:00:00 | DEBUG | 192.168.1.45 | /login | success"),
            new RawLine(2, "2024-03-15 02:30:00 | INFO | 192.168.1.45 | /login | success")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(0, result.unknownPatterns().size());
    }

    @Test
    void identifiesUnknownPatternInsideWindow() {
        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 02:30:00 | DEBUG | 192.168.1.45 | /login | success")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(1, result.unknownPatterns().size());
        assertEquals(1, result.unknownPatterns().get(0).lineNumber());
    }

    @Test
    void identifiesFlaggedEntries() {
        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 02:10:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(2,
                "2024-03-15 02:20:00 | ERROR | 192.168.1.46 | /admin | failed")
        );

        Map<String, Integer> rules = Map.of(
            "INFO", 1,
            "ERROR", 3
        );

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, null, null
        );

        assertEquals(1, result.flaggedEntries().size());
        assertEquals(3, result.flaggedEntries().get(0).severityScore());
    }

    @Test
    void countsEntriesByIp() {
        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 02:10:00 | ERROR | 192.168.1.45 | /admin | failed"),
            new RawLine(2,
                "2024-03-15 02:20:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(3,
                "2024-03-15 02:30:00 | ERROR | 192.168.1.46 | /admin | failed")
        );

        Map<String, Integer> rules = Map.of(
            "INFO", 1,
            "ERROR", 3
        );

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, null, null
        );

        assertEquals(2, result.suspiciousIps().size());

        assertEquals("192.168.1.45",
                result.suspiciousIps().get(0).ip());

        assertEquals(2,
                result.suspiciousIps().get(0).entryCount());
    }

    @Test
    void countsOnlyEntriesInsideWindowForIp() {
        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 01:00:00 | ERROR | 192.168.1.45 | /admin | failed"),
            new RawLine(2,
                "2024-03-15 02:10:00 | ERROR | 192.168.1.45 | /admin | failed"),
            new RawLine(3,
                "2024-03-15 02:20:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(4,
                "2024-03-15 04:00:00 | ERROR | 192.168.1.45 | /admin | failed")
        );

        Map<String, Integer> rules = Map.of(
            "INFO", 1,
            "ERROR", 3
        );

        LocalDateTime startTime =
                LocalDateTime.of(2024, 3, 15, 2, 0, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2024, 3, 15, 3, 0, 0);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, startTime, endTime
        );

        assertEquals(1, result.suspiciousIps().size());

        assertEquals(2,
                result.suspiciousIps().get(0).entryCount());
    }

    @Test
    void sortsFlaggedEntriesBySeverityDescending() {
        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 02:10:00 | ERROR | 192.168.1.45 | /admin | failed"),
            new RawLine(2,
                "2024-03-15 02:20:00 | CRITICAL | 192.168.1.46 | /admin | failed"),
            new RawLine(3,
                "2024-03-15 02:30:00 | WARNING | 192.168.1.47 | /admin | failed")
        );

        Map<String, Integer> rules = Map.of(
            "ERROR", 3,
            "CRITICAL", 5,
            "WARNING", 2
        );

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, null, null
        );

        assertEquals(2, result.flaggedEntries().size());

        assertEquals(5,
                result.flaggedEntries().get(0).severityScore());

        assertEquals(3,
                result.flaggedEntries().get(1).severityScore());
    }

    @Test
    void sortsSuspiciousIpsByEntryCountDescending() {
        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 02:10:00 | ERROR | 192.168.1.45 | /admin | failed"),
            new RawLine(2,
                "2024-03-15 02:20:00 | ERROR | 192.168.1.46 | /admin | failed"),
            new RawLine(3,
                "2024-03-15 02:30:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(4,
                "2024-03-15 02:40:00 | INFO | 192.168.1.45 | /login | success"),
            new RawLine(5,
                "2024-03-15 02:50:00 | INFO | 192.168.1.46 | /login | success")
        );

        Map<String, Integer> rules = Map.of(
            "INFO", 1,
            "ERROR", 3
        );

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, null, null
        );

        assertEquals(2, result.suspiciousIps().size());

        assertEquals("192.168.1.45",
                result.suspiciousIps().get(0).ip());

        assertEquals(3,
                result.suspiciousIps().get(0).entryCount());

        assertEquals("192.168.1.46",
                result.suspiciousIps().get(1).ip());

        assertEquals(2,
                result.suspiciousIps().get(1).entryCount());
    }

    @Test
    void handlesInvalidTimestamp() {
        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-13-45 25:70:90 | INFO | 192.168.1.45 | /login | success")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, null, null
        );

        assertEquals(1, result.malformedLines().size());

        assertEquals(1,
                result.malformedLines().get(0).lineNumber());
    }

    @Test
    void handlesIncorrectNumberOfFields() {
        List<RawLine> rawLines = List.of(
            new RawLine(1,
                "2024-03-15 02:10:00 | INFO | 192.168.1.45 | /login")
        );

        Map<String, Integer> rules = Map.of("INFO", 1);

        AnalysisResult result = Analyzer.analyze(
                rawLines, rules, null, null
        );

        assertEquals(1, result.malformedLines().size());

        assertEquals(1,
                result.malformedLines().get(0).lineNumber());
    }
}