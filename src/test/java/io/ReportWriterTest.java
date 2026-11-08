package io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.AnalysisResult;
import model.FlaggedEntry;
import model.MalformedLine;
import model.SuspiciousIp;
import model.UnknownPattern;

class ReportWriterTest {

    @TempDir
    Path tempDir;

    private AnalysisResult emptyResult() {
        return new AnalysisResult(
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
    }

    @Test
    void writesFullFileWhenTimeWindowIsNull() throws Exception {
        Path reportFile = tempDir.resolve("report.txt");

        ReportWriter.write(
            reportFile.toString(),
            emptyResult(),
            null,
            null
        );

        String report = Files.readString(reportFile);

        assertTrue(report.contains("Time Window: full file"));
    }

    @Test
    void writesTimeWindowWhenProvided() throws Exception {
        Path reportFile = tempDir.resolve("report.txt");

        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 2, 18, 30);

        ReportWriter.write(
            reportFile.toString(),
            emptyResult(),
            start,
            end
        );

        String report = Files.readString(reportFile);

        assertTrue(report.contains(
            "Time Window: 2026-01-01 10:00:00 to 2026-01-02 18:30:00"
        ));
    }

    @Test
    void writesActivitySummary() throws Exception {
        Path reportFile = tempDir.resolve("report.txt");

        AnalysisResult result = new AnalysisResult(
            Map.of("ERROR", 5, "WARNING", 3),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );

        ReportWriter.write(
            reportFile.toString(),
            result,
            null,
            null
        );

        String report = Files.readString(reportFile);

        assertTrue(report.contains("ERROR:  5"));
        assertTrue(report.contains("WARNING:  3"));
    }

    @Test
    void writesFlaggedEntries() throws Exception {
        Path reportFile = tempDir.resolve("report.txt");

        FlaggedEntry entry = new FlaggedEntry(
            "192.168.1.10 - Failed login attempt",
            8
        );

        AnalysisResult result = new AnalysisResult(
            Map.of(),
            List.of(entry),
            List.of(),
            List.of(),
            List.of()
        );

        ReportWriter.write(
            reportFile.toString(),
            result,
            null,
            null
        );

        String report = Files.readString(reportFile);

        assertTrue(report.contains(entry.originalLine()));
    }

    @Test
    void writesSuspiciousIps() throws Exception {
        Path reportFile = tempDir.resolve("report.txt");

        SuspiciousIp ip = new SuspiciousIp("192.168.1.10", 5);

        AnalysisResult result = new AnalysisResult(
            Map.of(),
            List.of(),
            List.of(ip),
            List.of(),
            List.of()
        );

        ReportWriter.write(
            reportFile.toString(),
            result,
            null,
            null
        );

        String report = Files.readString(reportFile);

        assertTrue(report.contains("192.168.1.10: 5 entries"));
    }

    @Test
    void writesUnknownPatterns() throws Exception {
        Path reportFile = tempDir.resolve("report.txt");

        UnknownPattern pattern = new UnknownPattern(
            12,
            "Unexpected log pattern"
        );

        AnalysisResult result = new AnalysisResult(
            Map.of(),
            List.of(),
            List.of(),
            List.of(pattern),
            List.of()
        );

        ReportWriter.write(
            reportFile.toString(),
            result,
            null,
            null
        );

        String report = Files.readString(reportFile);

        assertTrue(report.contains(
            "Line 12:  Unexpected log pattern"
        ));
    }

    @Test
    void writesMalformedLines() throws Exception {
        Path reportFile = tempDir.resolve("report.txt");

        MalformedLine line = new MalformedLine(
            15,
            "Invalid log entry"
        );

        AnalysisResult result = new AnalysisResult(
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(line)
        );

        ReportWriter.write(
            reportFile.toString(),
            result,
            null,
            null
        );

        String report = Files.readString(reportFile);

        assertTrue(report.contains("Line 15: Invalid log entry"));
    }

    @Test
    void writesNoneFoundForEmptySections() throws Exception {
        Path reportFile = tempDir.resolve("report.txt");

        ReportWriter.write(
            reportFile.toString(),
            emptyResult(),
            null,
            null
        );

        String report = Files.readString(reportFile);

        assertTrue(report.contains("--- Activity Summary ---"));
        assertTrue(report.contains("--- Flagged Entries ---"));
        assertTrue(report.contains("--- Suspicious Activity by IP ---"));
        assertTrue(report.contains("--- Unknown Patterns ---"));
        assertTrue(report.contains("--- Malformed Lines ---"));

        assertTrue(report.split("None found", -1).length - 1 == 5);
    }
}