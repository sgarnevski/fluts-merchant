package com.fluts.rest.evaluation;

import com.fluts.domain.FlutTradingOptimizer;
import com.fluts.domain.Scenario;
import com.fluts.domain.TradingResult;
import com.fluts.io.InputType;
import com.fluts.io.JsonScenarioParser;
import com.fluts.io.ScenarioParseException;
import com.fluts.io.ScenarioParser;
import com.fluts.io.TextScenarioParser;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;

/** Parses input, hands the scenarios to the {@link ScenarioRecorder} and solves them. */
public class EvaluationService {

    private final TextScenarioParser textParser;
    private final JsonScenarioParser jsonParser;
    private final FlutTradingOptimizer optimizer;
    private final ScenarioRecorder recorder;

    public EvaluationService(final TextScenarioParser textParser, final JsonScenarioParser jsonParser,
            final FlutTradingOptimizer optimizer, final ScenarioRecorder recorder) {
        this.textParser = textParser;
        this.jsonParser = jsonParser;
        this.optimizer = optimizer;
        this.recorder = recorder;
    }

    /** Evaluates a JSON request body in the normalized format. */
    public EvaluationResponse evaluateJson(final InputStream body) {
        return evaluate(jsonParser.parse(body), InputType.JSON);
    }

    /** Evaluates an uploaded file: {@code .json} (normalized format) or {@code .txt} (original format). */
    public EvaluationResponse evaluateFile(final String fileName, final InputStream content) {
        return evaluate(parserFor(fileName).parse(content), InputType.FILE);
    }

    private ScenarioParser parserFor(final String fileName) {
        final String lowerCaseName = fileName.toLowerCase(Locale.ROOT);
        if (lowerCaseName.endsWith(".json")) {
            return jsonParser;
        }
        if (lowerCaseName.endsWith(".txt")) {
            return textParser;
        }
        throw ScenarioParseException.of("Unsupported file '" + fileName + "': upload a .txt or .json file");
    }

    private EvaluationResponse evaluate(final List<Scenario> scenarios, final InputType inputType) {
        final List<Long> ids = recorder.record(scenarios, inputType);
        final List<EvaluationResult> results = IntStream.range(0, scenarios.size())
                .mapToObj(index -> toResult(scenarios.get(index), ids.isEmpty() ? null : ids.get(index)))
                .toList();
        return new EvaluationResponse(results);
    }

    private EvaluationResult toResult(final Scenario scenario, final @Nullable Long id) {
        final TradingResult result = optimizer.optimize(scenario);
        return new EvaluationResult(id, scenario.name(), result.maxProfit(), result.flutCounts());
    }
}
