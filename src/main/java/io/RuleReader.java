package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import exception.RuleFileException;

public class RuleReader {

    public static Map<String, Integer> readRules(String filePath) {

        Map<String, Integer> rules = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            String header = reader.readLine();

            if (!"level,severity_score".equals(header)) {
                throw new RuleFileException(
                        "Invalid CSV header in rules file: " + filePath
                );
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                String[] fields = line.split(",", -1);

                if (fields.length != 2) {
                    throw new RuleFileException(
                            "Invalid CSV row at line " + lineNumber
                                    + " in rules file: " + filePath
                    );
                }

                String level = fields[0].trim();
                int score;

                try {
                    score = Integer.parseInt(fields[1].trim());
                } catch (NumberFormatException e) {
                    throw new RuleFileException(
                            "Invalid severity_score at line " + lineNumber
                                    + " in rules file: " + filePath,
                            e
                    );
                }

                if (level.isEmpty()) {
                    throw new RuleFileException(
                            "Empty level at line " + lineNumber
                                    + " in rules file: " + filePath
                    );
                }

                if (rules.putIfAbsent(level, score) != null) {
                    throw new RuleFileException(
                            "Duplicate level '" + level
                                    + "' at line " + lineNumber
                                    + " in rules file: " + filePath
                    );
                }
            }

        } catch (IOException e) {
            throw new RuleFileException(
                    "Could not read rules file: " + filePath
                            + ". Reason: " + e.getMessage(),
                    e
            );
        }

        return rules;
    }
}