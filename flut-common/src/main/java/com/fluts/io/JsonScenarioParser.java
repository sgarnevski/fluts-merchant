package com.fluts.io;

import com.fluts.domain.Scenario;
import com.fluts.domain.Schuur;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses the normalized JSON format ({@link ScenariosInput}) and validates it into domain scenarios.
 *
 * <p>Strict on purpose: unknown properties, numbers written as strings, fractional prices and
 * trailing content are errors. Syntax errors carry the JSON line number; content errors name the
 * scenario and schuur by position.
 */
public final class JsonScenarioParser implements ScenarioParser {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .build();

    private final InputLimits limits;
    private final IntFunction<String> defaultName;

    /** Names unnamed scenarios "Scenario 1", "Scenario 2", ... by their position. */
    public JsonScenarioParser(final InputLimits limits) {
        this(limits, ScenarioParser.DEFAULT_NAME);
    }

    /**
     * @param defaultName the name of an unnamed scenario at a 1-based position in the input
     */
    public JsonScenarioParser(final InputLimits limits, final IntFunction<String> defaultName) {
        this.limits = limits;
        this.defaultName = defaultName;
    }

    @Override
    public List<Scenario> parse(final InputStream input) {
        return toScenarios(read(input));
    }

    /** Validates an already bound document (e.g. a REST request body) into domain scenarios. */
    public List<Scenario> toScenarios(final ScenariosInput document) {
        final List<ScenarioInput> scenarios = document.scenarios();
        if (scenarios == null || scenarios.isEmpty()) {
            throw ScenarioParseException.of("No scenarios given");
        }
        return IntStream.range(0, scenarios.size())
                .mapToObj(index -> toScenario(scenarios.get(index), index + 1))
                .toList();
    }

    private static ScenariosInput read(final InputStream input) {
        try {
            return MAPPER.readValue(input, ScenariosInput.class);
        } catch (final JacksonIOException e) {
            throw new UncheckedIOException(e.getCause());
        } catch (final JacksonException e) {
            throw invalidJson(e);
        }
    }

    private static ScenarioParseException invalidJson(final JacksonException e) {
        return ScenarioParseException.atLine(e.getLocation().getLineNr(), "Invalid JSON: " + e.getOriginalMessage(), e);
    }

    private Scenario toScenario(final ScenarioInput input, final int position) {
        final String label = "Scenario " + position;
        if (input == null) {
            throw ScenarioParseException.of(label + " is empty");
        }
        final List<SchuurInput> schuurs = input.schuurs();
        if (schuurs == null || schuurs.isEmpty()) {
            throw ScenarioParseException.of(label + " has no schuurs");
        }
        if (schuurs.size() > limits.maxSchuursPerScenario()) {
            throw ScenarioParseException.of(label + ": too many schuurs: " + schuurs.size()
                    + " (maximum is " + limits.maxSchuursPerScenario() + ")");
        }
        final List<Schuur> domainSchuurs = IntStream.range(0, schuurs.size())
                .mapToObj(index -> toSchuur(schuurs.get(index), label + ", schuur " + (index + 1)))
                .toList();
        final String name = input.name() == null || input.name().isBlank() ? defaultName.apply(position) : input.name();
        return new Scenario(name, domainSchuurs);
    }

    private Schuur toSchuur(final SchuurInput input, final String label) {
        if (input == null) {
            throw ScenarioParseException.of(label + " is empty");
        }
        final List<Integer> prices = input.boxPrices();
        if (prices == null) {
            throw ScenarioParseException.of(label + " has no boxPrices");
        }
        if (prices.size() > limits.maxBoxesPerSchuur()) {
            throw ScenarioParseException.of(label + ": too many boxes: " + prices.size()
                    + " (maximum is " + limits.maxBoxesPerSchuur() + ")");
        }
        for (final Integer price : prices) {
            if (price == null || price <= 0) {
                throw ScenarioParseException.of(label + ": box price must be positive, got " + price);
            }
        }
        return new Schuur(prices);
    }
}
