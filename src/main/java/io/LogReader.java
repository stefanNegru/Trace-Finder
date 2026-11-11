package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import exception.LogFileException;
import model.RawLine;

public class LogReader {

    public static List<RawLine> readLines(String filePath) {
        List<RawLine> lines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lines.add(new RawLine(lineNumber, line));
                lineNumber++;
            }
        } catch (IOException e) {
            throw new LogFileException(
                    "Could not read log file: " + filePath
                            + ". Reason: " + e.getMessage(),
                    e
            );
        }

        return lines;
    }
}