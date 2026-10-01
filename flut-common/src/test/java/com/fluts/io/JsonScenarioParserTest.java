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
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JsonScenarioParserTest {

    private final JsonScenarioParser parser = new JsonScenarioParser(InputLimits.DEFAULT);

    @Test
    void parsesTheSampleFile() throws IOException {
        try (InputStream input = Files.newInputStream(Path.of("../demo/input.json"))) {
            assertThat(parser.parse(input)).containsExactly(
                    new Scenario("Example 1", List.of(schuur(12, 3, 10, 7, 16, 5))),
                    new Scenario("Example 2", List.of(
                            schuur(7, 3, 11, 9, 10),
                            schuur(1, 2, 3, 4, 10, 16, 10, 4, 16))));
        }
    }

    @Test
    void namesUnnamedScenariosByPosition() {
        final List<Scenario> scenarios = parse("""
                {"scenarios": [
                  {"name": "First", "schuurs": [{"boxPrices": [1]}]},
                  {"schuurs": [{"boxPrices": [2]}]},
                  {"name": "  ", "schuurs": [{"boxPrices": []}]}
                ]}""");

        assertThat(scenarios).extracting(Scenario::name).containsExactly("First", "Scenario 2", "Scenario 3");
    }

    @Test
    void mapsAnAlreadyBoundDocument() {
        final ScenariosInput document = new ScenariosInput(List.of(
                new ScenarioInput(null, List.of(new SchuurInput(List.of(4, 11))))));

        assertThat(parser.toScenarios(document))
                .containsExactly(new Scenario("Scenario 1", List.of(schuur(4, 11))));
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @CsvSource(delimiter = '|', textBlock = """
            {}                                                                  | No scenarios given
            {"scenarios": []}                                                   | No scenarios given
            {"scenarios": [{"name": "a"}]}                                      | Scenario 1 has no schuurs
            {"scenarios": [{"schuurs": []}]}                                    | Scenario 1 has no schuurs
            {"scenarios": [{"schuurs": [{}]}]}                                  | Scenario 1, schuur 1 has no boxPrices
            {"scenarios": [{"schuurs": [{"boxPrices": [1, null]}]}]}            | Scenario 1, schuur 1: box price must be positive, got null
            {"scenarios": [{"schuurs": [{"boxPrices": [1]}, {"boxPrices": [0]}]}]} | Scenario 1, schuur 2: box price must be positive, got 0
            {"scenarios": [null]}                                               | Scenario 1 is empty
            {"scenarios": [{"schuurs": [null]}]}                                | Scenario 1, schuur 1 is empty
            """)
    void reportsInvalidContent(final String json, final String detail) {
        final ScenarioParseException error = catchThrowableOfType(ScenarioParseException.class, () -> parse(json));

        assertThat(error.line()).isEmpty();
        assertThat(error).hasMessage(detail);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '|', textBlock = """
            {"scenarios": [                                    | 1
            {"scenarios": [], "unknown": 1}                    | 1
            {"scenarios": [{"schuurs": [{"boxPrices": ["5"]}]}]} | 1
            {"scenarios": [{"schuurs": [{"boxPrices": [2.5]}]}]} | 1
            [1, 2]                                             | 1
            {"scenarios": []} {"scenarios": []}                | 1
            """)
    void reportsMalformedJsonWithLineNumber(final String json, final int line) {
        final ScenarioParseException error = catchThrowableOfType(ScenarioParseException.class, () -> parse(json));

        assertThat(error.line()).hasValue(line);
        assertThat(error.detail()).startsWith("Invalid JSON: ");
    }

    @Test
    void reportsTheLineOfMalformedJsonOnLaterLines() {
        final ScenarioParseException error = catchThrowableOfType(ScenarioParseException.class,
                () -> parse("{\n  \"scenarios\": [\n    {\"schuurs\": oops}\n  ]\n}"));

        assertThat(error.line()).hasValue(3);
    }

    @Test
    void rejectsMoreSchuursThanTheLimit() {
        final JsonScenarioParser limited = new JsonScenarioParser(new InputLimits(1, 5));

        final ScenarioParseException error = catchThrowableOfType(ScenarioParseException.class,
                () -> limited.parse(stream("{\"scenarios\": [{\"schuurs\": [{\"boxPrices\": []}, {\"boxPrices\": []}]}]}")));

        assertThat(error).hasMessage("Scenario 1: too many schuurs: 2 (maximum is 1)");
    }

    @Test
    void rejectsMoreBoxesThanTheLimit() {
        final JsonScenarioParser limited = new JsonScenarioParser(new InputLimits(1, 2));

        final ScenarioParseException error = catchThrowableOfType(ScenarioParseException.class,
                () -> limited.parse(stream("{\"scenarios\": [{\"schuurs\": [{\"boxPrices\": [1, 2, 3]}]}]}")));

        assertThat(error).hasMessage("Scenario 1, schuur 1: too many boxes: 3 (maximum is 2)");
    }

    @Test
    void namesUnnamedScenariosWithTheGivenFunction() {
        final JsonScenarioParser named = new JsonScenarioParser(InputLimits.DEFAULT, position -> "batch.json #" + position);

        assertThat(named.parse(stream("{\"scenarios\": [{\"schuurs\": [{\"boxPrices\": [1]}]}, "
                + "{\"name\": \"Own name\", \"schuurs\": [{\"boxPrices\": [2]}]}]}")))
                .extracting(Scenario::name).containsExactly("batch.json #1", "Own name");
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

    private List<Scenario> parse(final String json) {
        return parser.parse(stream(json));
    }

    private static InputStream stream(final String json) {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }

    private static Schuur schuur(final Integer... prices) {
        return new Schuur(Arrays.asList(prices));
    }
}
