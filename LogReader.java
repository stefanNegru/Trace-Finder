import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LogReader {

    public static List<RawLine> readLines(String filePath)
            throws IOException {

        List<RawLine> lines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lines.add(new RawLine(lineNumber, line));
                lineNumber++;
            }
        }

        return lines;
    }
}