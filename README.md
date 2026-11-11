# TraceFinder

TraceFinder is a Java command-line application that reads a log file, checks its entries against a rules file, and generates a report. It can analyse the entire log file or restrict the analysis to a specific time window.

## Requirements

- Java must be installed on your computer.
- Maven is required to build the project and run the tests.

### Building and running

Build the project using Maven:

```bash
mvn clean package
```

The executable JAR is generated in `target/tracefinder.jar`.

**Analyse the entire file:**

```bash
java -jar target/tracefinder.jar logs.txt rules.csv report.txt
```

**Analyse a specific time window:**

```bash
java -jar target/tracefinder.jar logs.txt rules.csv report.txt "2024-03-15 02:00:00" "2024-03-15 03:30:00"
```

The timestamp format is `uuuu-MM-dd HH:mm:ss`.

The start timestamp is inclusive, while the end timestamp is exclusive. Entries at the start are included; entries at the end are excluded.

The application accepts either three or five arguments.

| Argument     | Description                                  |
| ------------ | -------------------------------------------- |
| `logs.txt`   | The log file to analyse.                     |
| `rules.csv`  | The file containing severity scores.         |
| `report.txt` | The destination for the generated report.    |
| `start`      | The beginning of the time window (optional). |
| `end`        | The end of the time window (optional).       |

The start and end timestamps must follow this format:

```text
uuuu-MM-dd HH:mm:ss
```

For example: `2024-03-15 02:00:00`.

Both timestamps must be valid, and the start must not be later than the end.

Providing any number of arguments other than three or five stops the application with an explanatory error message and a non-zero exit code.

If either timestamp is invalid or the start is later than the end, the application stops cleanly and reports the problem.

When no time window is provided, the entire file is analysed.

## The log file

Each line should contain five fields separated by the `|` character:

```text
date and time | level | source IP | target | action
```

Example:

```text
2024-03-15 02:14:08 | INFO | 192.168.1.45 | /login | success
2024-03-15 02:30:12 | WARN | 192.168.1.50 | /admin | failed_login
2024-03-15 03:10:45 | ERROR | 192.168.1.60 | /database | connection_failed
```

A blank line, a line with an incorrect number of fields, or a line containing an invalid date and time is considered malformed.

Malformed lines are recorded in the report, and processing continues with the next line.

## The rules file

The rules file must be a CSV file beginning with the following header:

```csv
level,severity_score
```

Each subsequent row defines a log level and its severity score.

Example:

```csv
level,severity_score
INFO,1
WARN,3
ERROR,5
ALERT,9
```

Log levels are case-sensitive. For example, `INFO` and `info` are treated as different levels.

## Time window filtering

When five arguments are provided, TraceFinder analyses only entries within the selected time window.

The time window follows the **start-inclusive, end-exclusive** convention:

- An entry exactly at the start timestamp is included.
- An entry exactly at the end timestamp is excluded.
- Entries between the two timestamps are included.

In mathematical notation, the interval is `[start, end)`.

A window with identical start and end timestamps is valid but contains no entries.

All analysis results are based exclusively on entries inside the selected window, including:

- Activity counts.
- Flagged entries.
- Suspicious IP addresses and their totals.
- Unknown patterns.

Malformed lines are recorded regardless of the selected time window. Their original file line numbers are preserved, as are the original line numbers of unknown patterns.

Even when a time window contains no entries, TraceFinder generates a complete report with all five sections.

## The report

TraceFinder generates a report containing five sections:

1. **Activity Summary** — the number of occurrences of each known log level.
2. **Flagged Entries** — original log lines with a severity score of 3 or higher, ordered from highest to lowest severity.
3. **Suspicious Activity by IP** — IP addresses with at least one flagged entry, together with the number of valid entries associated with each IP.
4. **Unknown Patterns** — valid log entries containing a level that is not defined in the rules file.
5. **Malformed Lines** — lines that could not be parsed correctly.

All five sections are included in the report. Empty sections display `None found`.

The report header also specifies the time window used for the analysis.

Example:

```text
=== Log Analysis Report ===
Generated: 2024-03-15 09:00:00
Time Window: 2024-03-15 02:00:00 to 2024-03-15 03:30:00
```

When the entire file is analysed, the header displays:

```text
Time Window: full file
```

## Testing

The project includes automated tests covering the existing functionality and the time window filtering feature.

The boundary tests cover the following cases:

| Test case                          | Why it matters                                                              |
| ---------------------------------- | --------------------------------------------------------------------------- |
| Entry exactly at the start         | Verifies that the start boundary is inclusive.                              |
| Entry exactly at the end           | Verifies that the end boundary is exclusive.                                |
| Entry inside the window            | Confirms that entries within the selected interval are analysed.            |
| Entry outside the window           | Ensures that entries outside the interval are excluded.                     |
| Empty time window                  | Verifies that a report is still generated when no entries match.            |
| Equal start and end timestamps     | Checks that a zero-length window is valid and contains no entries.          |
| Reversed time window               | Ensures that a start timestamp later than the end is rejected.              |
| Invalid timestamp                  | Verifies that unreadable timestamps are handled cleanly.                    |
| Malformed lines outside the window | Ensures that malformed lines are still recorded regardless of filtering.    |
| Original line numbers              | Checks that filtering does not change the line numbers of unknown patterns. |

Existing TraceFinder tests are also maintained to ensure that the original functionality continues to work.

Run the test suite using:

```bash
mvn clean test
```

## Code coverage

Code coverage is measured using JaCoCo.

Generate the coverage report using:

```bash
mvn clean verify
```

The HTML report is generated at:

```text
target/site/jacoco/index.html
```

Open this file in a browser to inspect the coverage results.

**Coverage results:**

- Instruction coverage: [add actual result]
- Branch coverage: [add actual result]
- Covered instructions: [add actual count]
- Missed instructions: [add actual count]

Coverage indicates which parts of the code were executed during testing. It does not guarantee that every possible behaviour or edge case has been verified.

## Project structure

- `Main.java` — starts the application, validates command-line arguments, and connects the application components.
- `io/` — contains the classes responsible for reading input files and writing the report.
- `analysis/` — contains the log processing, time window filtering, and analysis logic.
- `model/` — contains the data structures used by the application.
- `src/test/` — contains automated tests for the application.

## Technologies

- Java
- Maven
- JUnit
- JaCoCo
