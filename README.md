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

The project includes automated tests using JUnit 5.

Run all tests with:

```bash
mvn test
```

The test suite covers the original TraceFinder functionality and the time window filtering feature.

### Time window edge cases

| Test case                          | Test method                                                          | Why it matters                                                                         |
| ---------------------------------- | -------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| Full-file analysis                 | `acceptsThreeArguments`                                              | Verifies that the application supports analysis without a time window.                 |
| Time-window analysis               | `acceptsFiveArguments`                                               | Verifies that the application accepts a time-window configuration.                     |
| Empty time window                  | `emptyTimeWindowProducesCompleteReportWithNoFindings`                | Verifies that a report is still generated when no entries match.                       |
| Equal start and end timestamps     | `equalStartAndEndProducesCompleteReportWithNoFindings`               | Checks that a zero-length window is valid and contains no entries.                     |
| Reversed time window               | `rejectsReversedTimeWindow`                                          | Ensures that a start timestamp later than the end is rejected.                         |
| Invalid start timestamp            | `rejectsInvalidStartTimestamp`                                       | Verifies that an invalid start timestamp is rejected.                                  |
| Invalid end timestamp              | `rejectsInvalidEndTimestamp`                                         | Verifies that an invalid end timestamp is rejected.                                    |
| Malformed lines outside the window | `timeWindowDoesNotChangeMalformedLines`                              | Ensures that malformed lines are recorded identically with and without time filtering. |
| Missing rules file                 | `rejectsMissingRulesFile`                                            | Verifies that a missing rules file is handled with an error.                           |
| Missing log file                   | `rejectsMissingLogFile`                                              | Verifies that a missing log file is handled with an error.                             |
| Invalid argument count             | `rejectsTwoArguments`, `rejectsFourArguments`, `rejectsSixArguments` | Ensures that unsupported CLI argument counts are rejected.                             |

Additional CLI tests verify argument validation, file handling, and report generation.

The test suite also covers parsing, rulebook matching, counting, grouping, flagging, and report formatting.

Run the tests from the project root using `mvn test`.

## Code coverage

Code coverage is measured using JaCoCo.

Generate the coverage report by running:

```bash
mvn clean verify
```

This command cleans the previous build, runs the test suite, and generates the JaCoCo report.

The HTML report is available at:

```text
target/site/jacoco/index.html
```

Open this file in a browser to inspect the coverage results.

### Coverage results

The generated JaCoCo report showed the following results:

| Metric               |  Coverage |
| -------------------- | --------: |
| Instruction coverage |       58% |
| Branch coverage      |       67% |
| Covered instructions | 555 / 942 |
| Covered branches     |   43 / 64 |
| Covered lines        | 142 / 262 |

### Coverage by package

| Package     | Instruction coverage | Branch coverage |
| ----------- | -------------------: | --------------: |
| `io`        |                  60% |             63% |
| `analysis`  |                  98% |            100% |
| `model`     |                  58% |             N/A |
| `default`   |                   0% |              0% |
| `exception` |                   0% |             N/A |

The `analysis` package has high coverage, including 100% branch coverage. The `io` and `model` packages still have some untested code.

The `default` and `exception` packages show 0% coverage because the CLI integration tests launch the application in separate Java processes. The JaCoCo agent used by Maven does not automatically instrument these child processes. Therefore, these tests verify CLI behaviour, but their execution is not included in the current coverage report.

Coverage indicates which code was executed during testing. It does not guarantee that every possible behaviour or edge case has been verified.

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
