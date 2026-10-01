package com.fluts.io;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * One scenario in the normalized JSON input.
 *
 * @param name    optional; defaults to "Scenario N" (1-based position)
 * @param schuurs the piles, at least one
 */
public record ScenarioInput(@Nullable String name, List<SchuurInput> schuurs) {
}
