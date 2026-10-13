package model;
public record FlaggedEntry(
        String originalLine,
        int severityScore
) {
}