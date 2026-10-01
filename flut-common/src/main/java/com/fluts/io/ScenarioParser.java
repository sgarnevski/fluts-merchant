package com.fluts.io;

import com.fluts.domain.Scenario;
import java.io.InputStream;
import java.util.List;
import java.util.function.IntFunction;

/** Reads scenarios from an input format. */
public interface ScenarioParser {

    /** The name of a scenario that has none in the input: "Scenario N", N being its 1-based position. */
    IntFunction<String> DEFAULT_NAME = position -> "Scenario " + position;

    /**
     * Parses all scenarios in the input. The stream is read but not closed.
     *
     * @throws ScenarioParseException  if the input is not valid for this format
     * @throws java.io.UncheckedIOException if the input cannot be read
     */
    List<Scenario> parse(InputStream input);
}
