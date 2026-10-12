import java.io.IOException;
import java.util.Map;

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

        System.out.println("Log file: " + logFile);
        System.out.println("Rules file: " + rulesFile);
        System.out.println("Report file: " + reportFile);

        
        try {
            Map<String, Integer> rules = RuleReader.readRules(rulesFile);
            System.out.println("Loaded rules: " + rules);
        } catch (IOException e) {
            System.err.println("Error reading rules file: " + e.getMessage());
            System.exit(1);
        }
    }
}