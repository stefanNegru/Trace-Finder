# TraceFinder

TraceFinder reads a log file, checks it using a rules file, and creates a report.

## What you need

Java must be installed on your computer. It must be able to run the provided `tracefinder.jar`.

## Run TraceFinder

Open a terminal in the project folder and run:

```bash
java -jar tracefinder.jar logs.txt rules.csv report.txt
```

The three file paths must be in this order:

1. `logs.txt` — the log file to check.
2. `rules.csv` — the file that gives each log level a score.
3. `report.txt` — the file where TraceFinder saves the report.

You can use different file names or locations. Keep the same order.

## The log file

Each line should have five parts, separated by the `|` character:

```text
date and time | level | source IP | target | action
```

Example:

```text
2024-03-15 02:14:08 | INFO | 192.168.1.45 | /login | success
```

A blank line, a line with the wrong number of parts, or a line with an invalid date and time is marked as malformed. TraceFinder records it and continues with the next line.

## The rules file

The rules file is a CSV file. It must start with this header:

```csv
level,severity_score
```

Each row gives a log level and its score:

```csv
INFO,1
WARN,3
ERROR,5
ALERT,9
```

The level must match exactly. For example, `INFO` and `info` are treated as different levels.

## The report

TraceFinder saves five sections to `report.txt`:

1. **Activity Summary** — how many times each known level appears.
2. **Flagged Entries** — original log lines with a score of 3 or higher. Higher scores appear first.
3. **Suspicious Activity by IP** — IP addresses with at least one flagged entry, and how many valid entries each IP has.
4. **Unknown Patterns** — valid log lines with a level that is missing from the rules file.
5. **Malformed Lines** — lines that could not be read correctly.

All five sections appear in the report. If a section has no results, it says `None found`.

## Project files

- `Main.java` starts the program and connects its parts.
- `io/` contains the code that reads files and writes the report.
- `analysis/` contains the code that checks the log entries.
- `model/` contains the data used by the program.