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

        //System.out.println("Log file: " + logFile);
        //System.out.println("Rules file: " + rulesFile);
        //System.out.println("Report file: " + reportFile);

        
        try {
            Map<String, Integer> rules = RuleReader.readRules(rulesFile);
            //System.out.println("Loaded rules: " + rules);

            //System.out.println("\n------------------------------------------------------");

            List<RawLine> rawLines = LogReader.readLines(logFile);
            AnalysisResult result = Analyzer.analyze(rawLines, rules);

            //System.out.println("Read " + rawLines.size() + " lines from log file.");
            //System.out.println(rawLines.get(1).lineNumber() + ": " + rawLines.get(1).content()); 
            

            ReportWriter.write(reportFile, result);
        } catch (IOException e) {
            System.err.println("Error reading rules file: " + e.getMessage());
            System.exit(1);
        }
    }
}