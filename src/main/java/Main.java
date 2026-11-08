import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;

import analysis.Analyzer;
import io.LogReader;
import io.ReportWriter;
import io.RuleReader;
import model.AnalysisResult;
import model.RawLine;

public class Main {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                .withResolverStyle(ResolverStyle.STRICT);
    public static void main(String[] args) {
        if (args.length != 3 && args.length != 5) {
            System.err.println(
                "Error: Expected either 3 arguments: " +
                "<log-file> <rules-file> <report-file>, or 5 arguments: " +
                "<log-file> <rules-file> <report-file> " +
                "<start-timestamp> <end-timestamp>"
            );
            System.exit(1);
        }

        String logFile = args[0];
        String rulesFile = args[1];
        String reportFile = args[2];

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        if (args.length == 5) {
            try {
                startTime = LocalDateTime.parse(args[3], FORMATTER);
                endTime = LocalDateTime.parse(args[4], FORMATTER);
            } catch (DateTimeParseException e) {
                System.err.println(
                    "Error: Invalid timestamp. Expected format: uuuu-MM-dd HH:mm:ss"
                );
                System.exit(1);
            }

            if (startTime.isAfter(endTime)) {
                System.err.println(
                    "Error: Start timestamp cannot be later than end timestamp."
                );
                System.exit(1);
            }
        }
        
        String currentOperation = "reading rules file";
        
        try {
            Map<String, Integer> rules = RuleReader.readRules(rulesFile);

            currentOperation = "reading log file";

            List<RawLine> rawLines = LogReader.readLines(logFile);

            AnalysisResult result = Analyzer.analyze(rawLines, rules, startTime, endTime);
            
            currentOperation = "writing report file";

            ReportWriter.write(reportFile, result, startTime, endTime);

        } catch (IOException e) {
            System.err.println("Error while " + currentOperation + ": " + e.getMessage());
            System.exit(1);
        }
    }
}