package com.fluts.io;

import com.fluts.domain.Scenario;
import com.fluts.domain.Schuur;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * Parses the original text format of the specification.
 *
 * <p>Per scenario a line {@code Z} (number of schuurs), then {@code Z} lines {@code E p1 … pE}
 * (number of boxes, then the prices top to bottom). A scenario with {@code Z = 0} ends the input.
 *
 * <p>Parsing is line based, as the specification describes it, so a pile line with too few or too
 * many prices is reported on that line instead of silently shifting all following numbers.
 * Blank lines, extra whitespace and {@code \r\n} line endings are accepted; a missing {@code 0}
 * terminator is accepted; anything after the terminator is ignored.
 */
public final class TextScenarioParser implements ScenarioParser {

    private static final int TERMINATOR = 0;

    private final InputLimits limits;
    private final IntFunction<String> defaultName;

    /** Names the scenarios "Scenario 1", "Scenario 2", ... (the text format has no names). */
    public TextScenarioParser(final InputLimits limits) {
        this(limits, ScenarioParser.DEFAULT_NAME);
    }

    /**
     * @param defaultName the name of the scenario at a 1-based position in the input
     */
    public TextScenarioParser(final InputLimits limits, final IntFunction<String> defaultName) {
        this.limits = limits;
        this.defaultName = defaultName;
    }

    @Override
    public List<Scenario> parse(final InputStream input) {
        final Iterator<Line> lines = contentLines(input);
        final List<Scenario> scenarios = new ArrayList<>();
        while (lines.hasNext()) {
            final Line header = lines.next();
            final int schuurCount = parseSchuurCount(header);
            if (schuurCount == TERMINATOR) {
                break;
            }
            final List<Schuur> schuurs = IntStream.range(0, schuurCount)
                    .mapToObj(index -> parseSchuur(nextPileLine(lines, header, schuurCount, index)))
                    .toList();
            scenarios.add(new Scenario(defaultName.apply(scenarios.size() + 1), schuurs));
        }
        return List.copyOf(scenarios);
    }

    private int parseSchuurCount(final Line header) {
        final String[] tokens = header.tokens();
        if (tokens.length != 1) {
            throw header.error("Expected only the number of schuurs, but the line has " + tokens.length + " values");
        }
        final int schuurCount = header.number(tokens[0]);
        if (schuurCount < 0) {
            throw header.error("Number of schuurs must not be negative, got " + schuurCount);
        }
        if (schuurCount > limits.maxSchuursPerScenario()) {
            throw header.error("Too many schuurs: " + schuurCount
                    + " (maximum is " + limits.maxSchuursPerScenario() + ")");
        }
        return schuurCount;
    }

    private static Line nextPileLine(final Iterator<Line> lines, final Line header, final int schuurCount,
            final int index) {
        if (!lines.hasNext()) {
            throw header.error("Expected " + schuurCount + " schuurs but the input ends after " + index);
        }
        return lines.next();
    }

    private Schuur parseSchuur(final Line line) {
        final String[] tokens = line.tokens();
        final int boxCount = line.number(tokens[0]);
        if (boxCount < 0) {
            throw line.error("Number of boxes must not be negative, got " + boxCount);
        }
        if (boxCount > limits.maxBoxesPerSchuur()) {
            throw line.error("Too many boxes: " + boxCount + " (maximum is " + limits.maxBoxesPerSchuur() + ")");
        }
        final int priceCount = tokens.length - 1;
        if (priceCount != boxCount) {
            throw line.error("Schuur declares " + boxCount + " boxes but lists " + priceCount + " prices");
        }
        final List<Integer> prices = Arrays.stream(tokens, 1, tokens.length)
                .map(line::price)
                .toList();
        return new Schuur(prices);
    }

    private static Iterator<Line> contentLines(final InputStream input) {
        final List<String> rawLines = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                .lines()
                .toList();
        return IntStream.range(0, rawLines.size())
                .filter(index -> !rawLines.get(index).isBlank())
                .mapToObj(index -> new Line(index + 1, rawLines.get(index).strip()))
                .iterator();
    }

    /** A non-blank input line with its 1-based line number. */
    private record Line(int number, String text) {

        String[] tokens() {
            return text.split("\\s+");
        }

        int number(final String token) {
            try {
                return Integer.parseInt(token);
            } catch (final NumberFormatException e) {
                throw ScenarioParseException.atLine(number, "'" + token + "' is not a whole number", e);
            }
        }

        int price(final String token) {
            final int price = number(token);
            if (price <= 0) {
                throw error("Box price must be positive, got " + price);
            }
            return price;
        }

        ScenarioParseException error(final String detail) {
            return ScenarioParseException.atLine(number, detail);
        }
    }
}
