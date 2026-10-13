package io;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportWriter {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void write(
            String filePath
            //AnalysisResult result,
            //List<MalformedLine> malformedLines
    ) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(
                Path.of(filePath),
                StandardCharsets.UTF_8
        )) {
            writer.write("=== Log Analysis Report ===");
            writer.newLine();

            writer.write("Generated: "
                    + LocalDateTime.now().format(FORMATTER));
            writer.newLine();
            writer.newLine();

            writeActivitySummary(writer);
            writeFlaggedEntries(writer/*, result.flaggedEntries()*/);
            writeSuspiciousIps(writer/*, result.suspiciousIps()*/);
            writeUnknownPatterns(writer);
            writeMalformedLines(writer);
        }
    }

    private static void writeActivitySummary(
            BufferedWriter writer
            //Map<String, Integer> summary
    ) throws IOException {

        writer.write("--- Activity Summary ---");
        writer.newLine();

        /*if (summary.isEmpty()) {
            writer.write("None found");
        } else {
            for (Map.Entry<String, Integer> entry : summary.entrySet()) {
                writer.write(entry.getKey() + ":  " + entry.getValue());
                writer.newLine();
            }
        }*/

        writer.newLine();
    }

    private static void writeFlaggedEntries(
            BufferedWriter writer
            //List<FlaggedEntry> entries
    ) throws IOException {

        writer.write("--- Flagged Entries ---");
        writer.newLine();

        /*if (entries.isEmpty()) {
            writer.write("None found");
            writer.newLine();
        } else {
            for (FlaggedEntry entry : entries) {
                writer.write(entry.originalLine());
                writer.newLine();
            }
        }*/

        writer.newLine();
    }

    private static void writeSuspiciousIps(
            BufferedWriter writer
            //List<SuspiciousIp> ips
    ) throws IOException {

        writer.write("--- Suspicious Activity by IP ---");
        writer.newLine();

        /*if (ips.isEmpty()) {
            writer.write("None found");
            writer.newLine();
        } else {
            for (SuspiciousIp ip : ips) {
                writer.write(ip.ip() + ": "
                        + ip.entryCount() + " entries");
                writer.newLine();
            }
        }*/

        writer.newLine();
    }

    private static void writeUnknownPatterns(
            BufferedWriter writer
            //List<UnknownPattern> patterns
    ) throws IOException {

        writer.write("--- Unknown Patterns ---");
        writer.newLine();

        /*if (patterns.isEmpty()) {
            writer.write("None found");
            writer.newLine();
        } else {
            for (UnknownPattern pattern : patterns) {
                writer.write("Line " + pattern.lineNumber()
                        + ":  " + pattern.content());
                writer.newLine();
            }
        }*/

        writer.newLine();
    }

    private static void writeMalformedLines(
            BufferedWriter writer
            //List<MalformedLine> lines
    ) throws IOException {

        writer.write("--- Malformed Lines ---");
        writer.newLine();

        /*if (lines.isEmpty()) {
            writer.write("None found");
            writer.newLine();
        } else {
            for (MalformedLine line : lines) {
                writer.write("Line " + line.lineNumber()
                        + ": " + line.content());
                writer.newLine();
            }
        }*/
    }
}
