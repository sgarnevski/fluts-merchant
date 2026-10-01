package com.fluts.io;

import java.util.List;

/**
 * The normalized JSON input document: {@code {"scenarios": [...]}}.
 *
 * <p>Fields may be missing or null in JSON; {@link JsonScenarioParser} validates them.
 */
public record ScenariosInput(List<ScenarioInput> scenarios) {
}
