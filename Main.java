import analysis.Analyzer;
import io.LogReader;
import io.ReportWriter;
import io.RuleReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import model.AnalysisResult;
import model.RawLine;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println(
                "Error: Expected 3 arguments: <log-file> <rules-file> <report-file>"
            );
            System.exit(1);
        }

        String logFile = args[0];
        String rulesFile = args[1];
        String reportFile = args[2];
        
        String currentOperation = "reading rules file";
        
        try {
            Map<String, Integer> rules = RuleReader.readRules(rulesFile);

            currentOperation = "reading log file";

            List<RawLine> rawLines = LogReader.readLines(logFile);
            
            AnalysisResult result = Analyzer.analyze(rawLines, rules);
            
            currentOperation = "writing report file";
            ReportWriter.write(reportFile, result);
        } catch (IOException e) {
            System.err.println("Error while " + currentOperation + ": " + e.getMessage());
            System.exit(1);
        }
    }
}