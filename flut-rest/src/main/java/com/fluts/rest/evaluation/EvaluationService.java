package com.fluts.rest.evaluation;

import com.fluts.domain.Scenario;
import com.fluts.io.InputType;
import com.fluts.io.JsonScenarioParser;
import com.fluts.io.ScenarioParseException;
import com.fluts.io.ScenarioParser;
import com.fluts.io.TextScenarioParser;
import com.fluts.trading.Evaluation;
import com.fluts.trading.TradingService;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;

/** Evaluates a request through the {@link TradingService} and hands the scenarios to the {@link ScenarioRecorder}. */
public class EvaluationService {

    private final TextScenarioParser textParser;
    private final JsonScenarioParser jsonParser;
    private final TradingService tradingService;
    private final ScenarioRecorder recorder;

    public EvaluationService(final TextScenarioParser textParser, final JsonScenarioParser jsonParser,
            final TradingService tradingService, final ScenarioRecorder recorder) {
        this.textParser = textParser;
        this.jsonParser = jsonParser;
        this.tradingService = tradingService;
        this.recorder = recorder;
    }

    /** Evaluates a JSON request body in the normalized format. */
    public EvaluationResponse evaluateJson(final InputStream body) {
        return respond(tradingService.evaluate(jsonParser, body), InputType.JSON);
    }

    /** Evaluates an uploaded file: {@code .json} (normalized format) or {@code .txt} (original format). */
    public EvaluationResponse evaluateFile(final String fileName, final InputStream content) {
        return respond(tradingService.evaluate(parserFor(fileName), content), InputType.FILE);
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

    private EvaluationResponse respond(final List<Evaluation> evaluations, final InputType inputType) {
        final List<Scenario> scenarios = evaluations.stream().map(Evaluation::scenario).toList();
        final List<Long> ids = recorder.record(scenarios, inputType);
        return new EvaluationResponse(IntStream.range(0, evaluations.size())
                .mapToObj(index -> toResult(evaluations.get(index), ids.isEmpty() ? null : ids.get(index)))
                .toList());
    }

    private static EvaluationResult toResult(final Evaluation evaluation, final @Nullable Long id) {
        return new EvaluationResult(id, evaluation.scenario().name(), evaluation.result().maxProfit(),
                evaluation.result().flutCounts());
    }
}
