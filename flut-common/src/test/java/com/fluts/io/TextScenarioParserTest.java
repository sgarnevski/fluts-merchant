package com.fluts.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.fluts.domain.Scenario;
import com.fluts.domain.Schuur;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TextScenarioParserTest {

    private final TextScenarioParser parser = new TextScenarioParser(InputLimits.DEFAULT);

    @Test
    void parsesTheSpecificationExample() throws IOException {
        try (InputStream input = Files.newInputStream(Path.of("../demo/input.txt"))) {
            final List<Scenario> scenarios = parser.parse(input);

            assertThat(scenarios).containsExactly(
                    new Scenario("Scenario 1", List.of(schuur(12, 3, 10, 7, 16, 5))),
                    new Scenario("Scenario 2", List.of(
                            schuur(7, 3, 11, 9, 10),
                            schuur(1, 2, 3, 4, 10, 16, 10, 4, 16))));
        }
    }

    @Test
    void acceptsBlankLinesExtraWhitespaceTabsAndWindowsLineEndings() {
        final List<Scenario> scenarios = parse("\r\n  2 \r\n\t3  1\t2 3\r\n\r\n 0\r\n0\r\n");

        assertThat(scenarios).containsExactly(new Scenario("Scenario 1", List.of(schuur(1, 2, 3), schuur())));
    }

    @Test
    void acceptsMissingTerminator() {
        assertThat(parse("1\n2 4 5\n")).containsExactly(new Scenario("Scenario 1", List.of(schuur(4, 5))));
    }

    @Test
    void ignoresEverythingAfterTheTerminator() {
        assertThat(parse("1\n1 4\n0\nnot even numbers\n")).hasSize(1);
    }

    @Test
    void namesScenariosWithTheGivenFunction() {
        final TextScenarioParser named = new TextScenarioParser(InputLimits.DEFAULT, position -> "batch.txt #" + position);

        assertThat(named.parse(stream("1\n1 4\n1\n1 5\n0\n")))
                .extracting(Scenario::name).containsExactly("batch.txt #1", "batch.txt #2");
    }

    @Test
    void emptyInputHasNoScenarios() {
        assertThat(parse("")).isEmpty();
        assertThat(parse("0\n")).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiter = '|', quoteCharacter = '"', textBlock = """
            1\\n3 1 2\\n                    | 2 | Schuur declares 3 boxes but lists 2 prices
            1\\n2 1 2 3\\n                  | 2 | Schuur declares 2 boxes but lists 3 prices
            1\\n3 1\\n2 3\\n                | 2 | Schuur declares 3 boxes but lists 1 prices
            2\\n1 5\\n                      | 1 | Expected 2 schuurs but the input ends after 1
            -1\\n                           | 1 | Number of schuurs must not be negative, got -1
            1\\n-2\\n                       | 2 | Number of boxes must not be negative, got -2
            1\\n2 5 0\\n                    | 2 | Box price must be positive, got 0
            1\\n2 5 -3\\n                   | 2 | Box price must be positive, got -3
            1\\n2 5 x\\n                    | 2 | 'x' is not a whole number
            1\\n1 2.5\\n                    | 2 | '2.5' is not a whole number
            abc\\n                          | 1 | 'abc' is not a whole number
            1 2\\n1 5\\n                    | 1 | Expected only the number of schuurs, but the line has 2 values
            1\\n1 99999999999\\n            | 2 | '99999999999' is not a whole number
            1\\n1 5\\n1\\n1 5 6\\n          | 4 | Schuur declares 1 boxes but lists 2 prices
            """)
    void reportsInvalidInputWithLineNumber(final String input, final int line, final String detail) {
        final ScenarioParseException error = catchThrowableOfType(ScenarioParseException.class,
                () -> parse(input.replace("\\n", "\n")));

        assertThat(error.line()).hasValue(line);
        assertThat(error.detail()).isEqualTo(detail);
        assertThat(error).hasMessage("Line " + line + ": " + detail);
    }

    @Test
    void rejectsMoreSchuursThanTheLimit() {
        final TextScenarioParser limited = new TextScenarioParser(new InputLimits(2, 5));

        final ScenarioParseException error = catchThrowableOfType(ScenarioParseException.class,
                () -> limited.parse(stream("3\n1 1\n1 1\n1 1\n")));

        assertThat(error).hasMessage("Line 1: Too many schuurs: 3 (maximum is 2)");
    }

    @Test
    void rejectsMoreBoxesThanTheLimit() {
        final TextScenarioParser limited = new TextScenarioParser(new InputLimits(2, 5));

        final ScenarioParseException error = catchThrowableOfType(ScenarioParseException.class,
                () -> limited.parse(stream("1\n6 1 1 1 1 1 1\n")));

        assertThat(error).hasMessage("Line 2: Too many boxes: 6 (maximum is 5)");
    }

    @Test
    void readFailuresAreNotParseErrors() {
        final InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("disk on fire");
            }
        };

        assertThat(catchThrowableOfType(UncheckedIOException.class, () -> parser.parse(broken)))
                .hasRootCauseMessage("disk on fire");
    }

    private List<Scenario> parse(final String input) {
        return parser.parse(stream(input));
    }

    private static InputStream stream(final String input) {
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

    private static Schuur schuur(final Integer... prices) {
        return new Schuur(List.of(prices));
    }
}
