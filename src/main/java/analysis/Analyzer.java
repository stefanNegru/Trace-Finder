package analysis;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.AnalysisResult;
import model.FlaggedEntry;
import model.MalformedLine;
import model.RawLine;
import model.SuspiciousIp;
import model.UnknownPattern;

public class Analyzer {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                    .withResolverStyle(ResolverStyle.STRICT);

    public static AnalysisResult analyze(
        List<RawLine> rawLines,
        Map<String, Integer> rules,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        Map<String, Integer> activitySummary = new LinkedHashMap<>();
        for (String ruleLevel : rules.keySet()) {
            activitySummary.put(ruleLevel, 0);
        }

        List<FlaggedEntry> flaggedEntries = new ArrayList<>();
        List<UnknownPattern> unknownPatterns = new ArrayList<>();
        List<MalformedLine> malformedLines = new ArrayList<>();
        List<SuspiciousIp> suspiciousIps = new ArrayList<>();

        Map<String, Integer> entriesByIp = new HashMap<>();
        Set<String> suspiciousIpSet = new HashSet<>();
            

        for (RawLine rawLine : rawLines) {
            String[] fields = rawLine.content().split("\\|", -1);

            if (fields.length != 5) {
                malformedLines.add(
                        new MalformedLine(rawLine.lineNumber(), rawLine.content())
                );
                continue;
            }

            String timestamp = fields[0].trim();
            String level = fields[1].trim();
            String sourceIp = fields[2].trim();
            //String target = fields[3].trim();
            //String action = fields[4].trim();

            LocalDateTime parsedTimestamp;
            
            try {
                parsedTimestamp = LocalDateTime.parse(
                        timestamp,
                        TIMESTAMP_FORMATTER
                );
            } catch (DateTimeParseException e) {
                malformedLines.add(new MalformedLine(rawLine.lineNumber(), rawLine.content()));
                continue;
            }

            if (startTime != null && parsedTimestamp.isBefore(startTime)) {
                continue;
            }

            if (endTime != null && !parsedTimestamp.isBefore(endTime)) {
                continue;
            }

            entriesByIp.put(
                    sourceIp,
                    entriesByIp.getOrDefault(sourceIp, 0) + 1
            );

            if (!rules.containsKey(level)) {
                unknownPatterns.add(
                        new UnknownPattern(rawLine.lineNumber(), rawLine.content())
                );
                continue;
            }

            int count = activitySummary.getOrDefault(level, 0);
            activitySummary.put(level, count + 1);

            int severityScore = rules.get(level);

            if (severityScore >= 3) {
                flaggedEntries.add(
                        new FlaggedEntry(rawLine.content(), severityScore)
                );
                suspiciousIpSet.add(sourceIp);
            }
        }

        flaggedEntries.sort(
            Comparator.comparingInt(
                FlaggedEntry::severityScore
            ).reversed()
        );

        for (String ip : suspiciousIpSet) {
            suspiciousIps.add(
                    new SuspiciousIp(
                            ip,
                            entriesByIp.get(ip)
                    )
            );
        }

        suspiciousIps.sort(
                Comparator.comparingInt(
                        SuspiciousIp::entryCount
                ).reversed()
        );

        return new AnalysisResult(
                activitySummary,
                flaggedEntries,
                suspiciousIps,
                unknownPatterns,
                malformedLines
        );
    }
}
