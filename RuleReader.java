
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class RuleReader {

    public static Map<String, Integer> readRules(String filePath)
            throws IOException {

        Map<String, Integer> rules = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            String header = reader.readLine();

            if (!"level,severity_score".equals(header)) {
                throw new IOException(
                    "Invalid CSV header in rules file: " + filePath
                );
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);

                if (fields.length != 2) {
                    throw new IOException(
                        "Invalid CSV row in rules file: " + line
                    );
                }

                String level = fields[0].trim();
                int score;

                try {
                    score = Integer.parseInt(fields[1].trim());
                } catch (NumberFormatException e) {
                    throw new IOException(
                        "Invalid severity_score in rules file: " + line, e
                    );
                }

                if (level.isEmpty()) {
                    throw new IOException(
                        "Empty level in rules file: " + line
                    );
                }

                if (rules.putIfAbsent(level, score) != null) {
                    throw new IOException(
                        "Duplicate level in rules file: " + level
                    );
                }
            }
        }

        return rules;
    }
}