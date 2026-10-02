# Trading of Fluts

A merchant buys fluts from piles ("schuurs") of boxes, always from the top down, and sells each flut
for 10 florins. For every scenario this project finds the **maximum profit** and **every number of
fluts** that reaches it (ascending, at most the 10 smallest), exactly as the specification asks.

Two ways to use it, both Java 21 only (Spring Boot 4.1, Maven wrapper included):

| Way | Module |
|---|---|
| Console app with the specification's input and output | `flut-cli` |
| REST API with Swagger UI | `flut-rest` |

The `demo/` folder holds files to try it with:

| File | What it is |
|---|---|
| `demo/input.txt` | the specification's example input (two scenarios) |
| `demo/output.txt` | the specification's expected output for it |
| `demo/input.json` | the same two scenarios in the JSON format |
| `demo/large.txt` | one scenario with 7 piles of 60 boxes |
| `demo/invalid.txt` | a pile that announces 3 boxes but lists 2 — shows an error with its line number |

## 1. Console app

```bash
./mvnw -q -pl flut-cli -am package          # builds and tests; add -DskipTests to only build

java -jar flut-cli/target/flut-cli.jar < demo/input.txt      # stdin, as the specification suggests
java -jar flut-cli/target/flut-cli.jar demo/input.txt        # same, file as argument
java -jar flut-cli/target/flut-cli.jar demo/large.txt        # 7 piles of 60 boxes: profit 1806 with 420 fluts
java -jar flut-cli/target/flut-cli.jar demo/invalid.txt      # -> stderr "Invalid input: Line 2: Schuur declares 3 boxes but lists 2 prices", exit code 1
java -jar flut-cli/target/flut-cli.jar demo/missing.txt      # -> stderr "Cannot read input: ...", exit code 2
```

Output (stdout carries nothing else, so it can be piped or diffed):

```
schuurs 1
Maximum profit is 8.
Number of fluts to buy: 4

schuurs 2
Maximum profit is 40.
Number of fluts to buy: 6 7 8 9 10 12 13
```

Exit codes: `0` ok, `1` invalid input (message with line number on stderr), `2` file not readable or
wrong usage. Check it against the expected output:
`java -jar flut-cli/target/flut-cli.jar < demo/input.txt | diff - demo/output.txt`

## 2. REST API

```bash
./mvnw -q -pl flut-rest -am package
java -jar flut-rest/target/flut-rest-exec.jar
```

Swagger UI: <http://localhost:8080/swagger-ui.html> (OpenAPI: <http://localhost:8080/v3/api-docs>)

```bash
# Original text format as a file, answer in the original output format
curl -F file=@demo/input.txt -H 'Accept: text/plain' localhost:8080/api/evaluations

# JSON in, JSON out
curl -H 'Content-Type: application/json' --data @demo/input.json localhost:8080/api/evaluations
# {"results":[{"scenario":"Example 1","maxProfit":8,"flutCounts":[4]},
#             {"scenario":"Example 2","maxProfit":40,"flutCounts":[6,7,8,9,10,12,13]}]}

# Invalid input: problem details (RFC 9457) with the line number
curl -F file=@demo/invalid.txt localhost:8080/api/evaluations
# {"detail":"Schuur declares 3 boxes but lists 2 prices","instance":"/api/evaluations","status":400,"title":"Invalid input","line":2}
```

`POST /api/evaluations` takes `application/json` (the JSON format below) or `multipart/form-data`
with a part `file` (`.txt` in the original format, or `.json`), and answers JSON or, with
`Accept: text/plain`, the original output format. Requests and uploads are limited to 1 MB.

## Input formats

**Original text** (`.txt`, and the console app), as in the specification: per scenario a line `Z`
(number of schuurs), then `Z` lines `E p1 … pE` (number of boxes, then the prices from top to bottom);
`Z = 0` ends the input. Rules for input the specification does not describe:

- accepted: blank lines, extra spaces or tabs, Windows line endings, a missing `0` at the end,
  empty piles (a line `0`); anything after the terminating `0` is ignored;
- errors, with the line number: a pile line whose number of prices differs from its `E`, a pile
  split over several lines, fewer pile lines than `Z`, negative numbers, prices `<= 0`, non-numbers.

**JSON** (`.json` upload or JSON body) — no counts, no terminator; `name` is optional and defaults
to "Scenario N":

```json
{ "scenarios": [ { "name": "Example 2",
                   "schuurs": [ { "boxPrices": [7, 3, 11, 9, 10] },
                                { "boxPrices": [1, 2, 3, 4, 10, 16, 10, 4, 16] } ] } ] }
```

Limits (configurable, `flut.limits.*`): 1,000 schuurs per scenario, 10,000 boxes per schuur.

The rules of the trade are configuration too (`flut.trading.*`), with the specification's values as
defaults: a flut sells for `selling-price=10` florins, and the `max-reported-counts=10` smallest
numbers of fluts are reported. For example
`java -jar flut-cli/target/flut-cli.jar --flut.trading.selling-price=15 < demo/input.txt`.

## How it works

For one pile, buying the top `k` boxes earns `Σ (10 − price)` over those boxes, so the code walks
the pile once with a running profit and remembers the best value and every `k` that reaches it
(`k = 0` included: buying nothing). Piles are independent: the maximum profit is the sum of the
piles' maxima, and the optimal flut counts are all sums of one optimal `k` per pile. Only the 10
smallest are reported, and keeping just the 10 smallest partial sums after each pile is enough to
find them (at most 10 × 10 sums per pile). Profits are `long`, so huge prices cannot overflow;
the whole thing is linear in the number of boxes. See `FlutTradingOptimizer`, 60 lines of plain loops;
the selling price and the number of reported counts come in as `TradingRules`.

Example 2: pile A reaches its maximum 10 at `k ∈ {2, 4, 5}`, pile B its maximum 30 at
`k ∈ {4, 5, 8}` → maximum profit 40, flut counts `{2,4,5} + {4,5,8}` = 6 7 8 9 10 12 13.

## Project structure

```
flut-core     domain records, TradingRules + FlutTradingOptimizer — the algorithm only, no dependencies
flut-common   text / JSON parsers, the text formatter and TradingService (parse → optimize) — shared logic, no Spring
flut-cli      Spring Boot console app (no web)
flut-rest     Spring Boot REST app: POST /api/evaluations, problem details, Swagger UI
demo/         files to try things with
```

Dependencies point inward: `flut-rest → flut-common → flut-core` and `flut-cli → flut-common → flut-core`.
The domain and the parsers are plain Java and unit-tested on their own; Spring only wires them.
Both apps evaluate through the same `TradingService` (parse the input, optimize every scenario), so
the console app and the REST API cannot drift apart; the optimizer stays the pure algorithm.

Design choices worth knowing:

- **Parsing is line-based, as the specification describes.** A number stream would silently read
  an extra price as the next pile's count; line-based parsing reports every mismatch on its line.
- **One web layer, pluggable storage.** `flut-rest` exposes a `ScenarioRecorder` port; this app
  keeps nothing, an app with a database would plug in its own without touching the controllers.
- **Clean stdout in the console app:** no banner, logs on stderr, so the output can be piped.

## Development

```bash
./mvnw verify          # all modules; no Docker needed
```

`verify` fails when a module is below **100 % line and branch coverage** (JaCoCo) or when a
variable is not **`final`** (PMD, `config/pmd/ruleset.xml`; `flut-core` uses `ruleset-core.xml`,
which allows reassignable locals so the algorithm reads as loops with running totals).
Coverage reports: `<module>/target/site/jacoco/index.html`.
