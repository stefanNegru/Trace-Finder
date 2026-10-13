package model;
public record SuspiciousIp(
        String ip,
        int entryCount
) {
}