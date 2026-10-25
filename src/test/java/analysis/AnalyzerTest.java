package analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import model.AnalysisResult;
import model.RawLine;
import org.junit.jupiter.api.Test;

class AnalyzerTest {
    @Test
    void countsKnownLevel() {
        
        List<RawLine> rawLines = List.of(
            new RawLine(1, "2024-03-15 02:14:08 | INFO | 192.168.1.45 | /login | success")
        );
        Map<String, Integer> rules = Map.of("INFO", 1);
        AnalysisResult result = Analyzer.analyze(rawLines, rules);

        assertEquals(1, result.activitySummary().get("INFO"));
    }
}