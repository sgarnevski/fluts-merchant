package com.fluts.rest.evaluation;

import com.fluts.domain.Scenario;
import com.fluts.io.InputType;
import java.util.List;

/**
 * Port for keeping evaluated scenarios, so that an app with a database can store every scenario it
 * evaluates without changing the web layer. This app keeps nothing ({@link NoOpScenarioRecorder}).
 */
public interface ScenarioRecorder {

    /**
     * Records the scenarios.
     *
     * @return the ids of the saved scenarios, in the same order, or an empty list when nothing is saved
     */
    List<Long> record(List<Scenario> scenarios, InputType inputType);
}
