
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

    private Path createRulesFile() throws IOException {
        Path rulesFile = tempDir.resolve("rules.csv");

        Files.writeString(
            rulesFile,
            "level,severity_score\n" +
            "ERROR,5\n" +
            "WARN,3\n" +
            "INFO,1\n"
        );

        return rulesFile;
    }

    private Path createReportFile() {
        return tempDir.resolve("report.txt");
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
    void acceptsEqualStartAndEndTimestamps() throws Exception {
        Path logFile = createLogFile();
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
}
