
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    @TempDir
    Path tempDir;

    private record ProcessResult(
        int exitCode,
        String stdout,
        String stderr
    ) {}

    private ProcessResult runMain(String... args)
            throws IOException, InterruptedException {

        String javaExecutable = Path.of(
            System.getProperty("java.home"),
            "bin",
            "java"
        ).toString();

        ProcessBuilder builder = new ProcessBuilder(
            javaExecutable,
            "-cp",
            System.getProperty("java.class.path"),
            "Main"
        );

        for (String arg : args) {
            builder.command().add(arg);
        }

        Process process = builder.start();

        boolean finished = process.waitFor(10, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Main process timed out");
        }

        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr = new String(process.getErrorStream().readAllBytes());

        return new ProcessResult(
            process.exitValue(),
            stdout,
            stderr
        );
    }

    private Path createLogFile() throws IOException {
        Path logFile = tempDir.resolve("logs.txt");

        Files.writeString(
            logFile,
            "2024-03-15 02:30:00 ERROR 192.168.1.10 Test error\n"
        );

        return logFile;
    }

    private Path createWindowEdgeCaseLogFile() throws IOException {
    Path logFile = tempDir.resolve("edge-case-logs.txt");

    Files.writeString(
        logFile,
        "2024-03-15 02:30:00 | ERROR | 192.168.1.10 | /login | failed\n" +
        "2024-03-15 02:30:00 | DEBUG | 192.168.1.20 | /test | unknown\n" +
        "this is a malformed line\n"
    );

    return logFile;
}

    private Path createRulesFile() throws IOException {
        Path rulesFile = tempDir.resolve("rules.csv");

        Files.writeString(
            rulesFile,
            "level,severity_score\n" +
            "ERROR,5\n" +
            "WARN,3\n" +
            "INFO,1\n" +
            "ALERT,0\n"
        );

        return rulesFile;
    }

    private Path createReportFile() {
        return tempDir.resolve("report.txt");
    }

    
    private void assertEmptyFindings(String report) {
        assertTrue(report.contains("--- Activity Summary ---"));
        assertTrue(report.contains("--- Flagged Entries ---"));
        assertTrue(report.contains("--- Suspicious Activity by IP ---"));
        assertTrue(report.contains("--- Unknown Patterns ---"));
        assertTrue(report.contains("--- Malformed Lines ---"));

        assertSectionContains(report, "--- Activity Summary ---", "None found");
        assertSectionContains(report, "--- Flagged Entries ---", "None found");
        assertSectionContains(report, "--- Suspicious Activity by IP ---", "None found");
        assertSectionContains(report, "--- Unknown Patterns ---", "None found");

        assertSectionContains(
            report,
            "--- Malformed Lines ---",
            "Line 3: this is a malformed line"
        );
    }


    /*private void assertSectionContains(
            String report,
            String sectionHeader,
            String expectedContent) {

        int start = report.indexOf(sectionHeader);

        assertTrue(start >= 0, "Missing section: " + sectionHeader);

        start += sectionHeader.length();

        int end = report.indexOf("\n\n--- ", start);

        String section = report.substring(
            start,
            end >= 0 ? end : report.length()
        );

        assertTrue(
            section.contains(expectedContent),
            "Expected content not found in section " + sectionHeader
        );
    }*/


    private String getSectionContent(
            String report,
            String sectionHeader) {

        int start = report.indexOf(sectionHeader);

        assertTrue(start >= 0, "Missing section: " + sectionHeader);

        start += sectionHeader.length();

        int end = report.indexOf("\n\n--- ", start);

        return report.substring(
            start,
            end >= 0 ? end : report.length()
        ).strip();
    }



    private void assertSectionContains(
            String report,
            String sectionHeader,
            String expectedContent) {

        String section = getSectionContent(report, sectionHeader);

        assertTrue(
            section.contains(expectedContent),
            "Expected content not found in section " + sectionHeader
        );
    }


    @Test
    void rejectsTwoArguments() throws Exception {
        ProcessResult result = runMain("logs.txt", "rules.csv");

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("Expected either 3 arguments"));
    }

    @Test
    void rejectsFourArguments() throws Exception {
        ProcessResult result = runMain(
            "logs.txt", "rules.csv", "report.txt", "2024-03-15 02:00:00"
        );

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("Expected either 3 arguments"));
    }

    @Test
    void rejectsSixArguments() throws Exception {
        ProcessResult result = runMain(
            "logs.txt", "rules.csv", "report.txt",
            "2024-03-15 02:00:00",
            "2024-03-15 03:00:00",
            "extra"
        );

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("Expected either 3 arguments"));
    }

    @Test
    void acceptsThreeArguments() throws Exception {
        Path logFile = createLogFile();
        Path rulesFile = createRulesFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            logFile.toString(),
            rulesFile.toString(),
            reportFile.toString()
        );

        assertEquals(0, result.exitCode());
        assertTrue(Files.exists(reportFile));

        String report = Files.readString(reportFile);
        assertTrue(report.contains("Time Window: full file"));
    }

    @Test
    void acceptsFiveArguments() throws Exception {
        Path logFile = createLogFile();
        Path rulesFile = createRulesFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            logFile.toString(),
            rulesFile.toString(),
            reportFile.toString(),
            "2024-03-15 02:00:00",
            "2024-03-15 03:00:00"
        );

        assertEquals(0, result.exitCode());
        assertTrue(Files.exists(reportFile));

        String report = Files.readString(reportFile);

        assertTrue(
            report.contains(
                "Time Window: 2024-03-15 02:00:00 to 2024-03-15 03:00:00"
            )
        );
    }

    @Test
    void rejectsInvalidStartTimestamp() throws Exception {
        Path logFile = createLogFile();
        Path rulesFile = createRulesFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            logFile.toString(),
            rulesFile.toString(),
            reportFile.toString(),
            "invalid-date",
            "2024-03-15 03:00:00"
        );

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("Invalid timestamp"));
    }

    @Test
    void rejectsInvalidEndTimestamp() throws Exception {
        Path logFile = createLogFile();
        Path rulesFile = createRulesFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            logFile.toString(),
            rulesFile.toString(),
            reportFile.toString(),
            "2024-03-15 02:00:00",
            "invalid-date"
        );

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("Invalid timestamp"));
    }

    @Test
    void rejectsReversedTimeWindow() throws Exception {
        Path logFile = createLogFile();
        Path rulesFile = createRulesFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            logFile.toString(),
            rulesFile.toString(),
            reportFile.toString(),
            "2024-03-15 04:00:00",
            "2024-03-15 02:00:00"
        );

        assertEquals(1, result.exitCode());
        assertTrue(
            result.stderr().contains(
                "Start timestamp cannot be later than end timestamp"
            )
        );
    }

    @Test
    void equalStartAndEndProducesCompleteReportWithNoFindings() throws Exception {
        Path logFile = createWindowEdgeCaseLogFile();
        Path rulesFile = createRulesFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            logFile.toString(),
            rulesFile.toString(),
            reportFile.toString(),
            "2024-03-15 02:30:00",
            "2024-03-15 02:30:00"
        );

        assertEquals(0, result.exitCode());
        assertTrue(Files.exists(reportFile));

        String report = Files.readString(reportFile);

        assertTrue(
            report.contains(
                "Time Window: 2024-03-15 02:30:00 to 2024-03-15 02:30:00"
            )
        );

        assertEmptyFindings(report);
    }

    @Test
    void rejectsMissingRulesFile() throws Exception {
        Path logFile = createLogFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            logFile.toString(),
            tempDir.resolve("missing-rules.csv").toString(),
            reportFile.toString()
        );

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("reading rules file"));
    }

    @Test
    void rejectsMissingLogFile() throws Exception {
        Path rulesFile = createRulesFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            tempDir.resolve("missing-logs.txt").toString(),
            rulesFile.toString(),
            reportFile.toString()
        );

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("reading log file"));
    }

    @Test
    void emptyTimeWindowProducesCompleteReportWithNoFindings() throws Exception {
        Path logFile = createWindowEdgeCaseLogFile();
        Path rulesFile = createRulesFile();
        Path reportFile = createReportFile();

        ProcessResult result = runMain(
            logFile.toString(),
            rulesFile.toString(),
            reportFile.toString(),
            "2024-03-15 04:00:00",
            "2024-03-15 05:00:00"
        );

        assertEquals(0, result.exitCode());
        assertTrue(Files.exists(reportFile));

        String report = Files.readString(reportFile);

        assertTrue(
            report.contains(
                "Time Window: 2024-03-15 04:00:00 to 2024-03-15 05:00:00"
            )
        );

        assertEmptyFindings(report);
    }


    @Test
    void timeWindowDoesNotChangeMalformedLines() throws Exception {
        Path logFile = createWindowEdgeCaseLogFile();
        Path rulesFile = createRulesFile();

        Path fullReportFile = tempDir.resolve("full-report.txt");
        Path windowReportFile = tempDir.resolve("window-report.txt");

        // Run without a time window
        ProcessResult fullResult = runMain(
            logFile.toString(),
            rulesFile.toString(),
            fullReportFile.toString()
        );

        // Run with a time window
        ProcessResult windowResult = runMain(
            logFile.toString(),
            rulesFile.toString(),
            windowReportFile.toString(),
            "2024-03-15 02:30:00",
            "2024-03-15 02:31:00"
        );

        assertEquals(0, fullResult.exitCode());
        assertEquals(0, windowResult.exitCode());

        assertTrue(Files.exists(fullReportFile));
        assertTrue(Files.exists(windowReportFile));

        String fullReport = Files.readString(fullReportFile);
        String windowReport = Files.readString(windowReportFile);

        String fullMalformedSection = getSectionContent(
            fullReport,
            "--- Malformed Lines ---"
        );

        String windowMalformedSection = getSectionContent(
            windowReport,
            "--- Malformed Lines ---"
        );

        // Malformed lines must be identical in both reports
        assertEquals(fullMalformedSection, windowMalformedSection);

        // Verify that malformed lines are actually present
        assertTrue(
            fullMalformedSection.contains(
                "Line 3: this is a malformed line"
            )
        );
    }

}
