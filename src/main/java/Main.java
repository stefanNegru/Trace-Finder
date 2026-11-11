import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;

import analysis.Analyzer;
import exception.InvalidArgumentsException;
import exception.UnrecoverableException;
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
        try {
            if (args.length != 3 && args.length != 5) {
                throw new InvalidArgumentsException(
                        "Expected either 3 arguments: " +
                        "<log-file> <rules-file> <report-file>, or 5 arguments: " +
                        "<log-file> <rules-file> <report-file> " +
                        "<start-timestamp> <end-timestamp>"
                );
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
                    throw new InvalidArgumentsException(
                            "Invalid timestamp. Expected format: uuuu-MM-dd HH:mm:ss",
                            e
                    );
                }

                if (startTime.isAfter(endTime)) {
                    throw new InvalidArgumentsException(
                            "Start timestamp cannot be later than end timestamp."
                    );
                }
            }

            Map<String, Integer> rules = RuleReader.readRules(rulesFile);

            List<RawLine> rawLines = LogReader.readLines(logFile);

            AnalysisResult result = Analyzer.analyze(
                    rawLines, rules, startTime, endTime
            );

            ReportWriter.write(
                    reportFile, result, startTime, endTime
            );

        } catch (UnrecoverableException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);

        } /*catch (IOException e) {
            // Temporar: LogReader și ReportWriter încă folosesc IOException.
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }*/
    }
}