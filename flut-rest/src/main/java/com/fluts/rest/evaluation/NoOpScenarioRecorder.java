package com.fluts.rest.evaluation;

import com.fluts.domain.Scenario;
import com.fluts.io.InputType;
import java.util.List;

/** Keeps nothing; used by the stateless app. */
public final class NoOpScenarioRecorder implements ScenarioRecorder {

    @Override
    public List<Long> record(final List<Scenario> scenarios, final InputType inputType) {
        return List.of();
    }
}
