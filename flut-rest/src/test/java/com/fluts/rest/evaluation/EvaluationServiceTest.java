package com.fluts.rest.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fluts.domain.FlutTradingOptimizer;
import com.fluts.domain.TradingRules;
import com.fluts.domain.Scenario;
import com.fluts.io.InputLimits;
import com.fluts.io.InputType;
import com.fluts.io.JsonScenarioParser;
import com.fluts.io.ScenarioParseException;
import com.fluts.io.TextScenarioParser;
import com.fluts.trading.TradingService;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class EvaluationServiceTest {

    private static final String TEXT = "1\n6 12 3 10 7 16 5\n0\n";
    private static final String JSON = "{\"scenarios\": [{\"name\": \"One\", \"schuurs\": [{\"boxPrices\": [12, 3, 10, 7, 16, 5]}]}]}";

    @Test
    void evaluatesJsonWithoutIdsWhenNothingIsSaved() {
        final EvaluationService service = service(new NoOpScenarioRecorder());

        final EvaluationResponse response = service.evaluateJson(stream(JSON));

        assertThat(response.results()).containsExactly(new EvaluationResult(null, "One", 8, List.of(4)));
    }

    @Test
    void evaluatesTextFilesAndReturnsTheSavedIds() {
        final CapturingRecorder recorder = new CapturingRecorder();

        final EvaluationResponse response = service(recorder).evaluateFile("input.TXT", stream(TEXT));

        assertThat(response.results()).containsExactly(new EvaluationResult(100L, "Scenario 1", 8, List.of(4)));
        assertThat(recorder.inputTypes).containsExactly(InputType.FILE);
    }

    @Test
    void evaluatesJsonFiles() {
        final CapturingRecorder recorder = new CapturingRecorder();

        final EvaluationResponse response = service(recorder).evaluateFile("scenarios.json", stream(JSON));

        assertThat(response.results()).extracting(EvaluationResult::scenario).containsExactly("One");
        assertThat(recorder.inputTypes).containsExactly(InputType.FILE);
    }

    @Test
    void recordsJsonBodiesAsJson() {
        final CapturingRecorder recorder = new CapturingRecorder();

        service(recorder).evaluateJson(stream(JSON));

        assertThat(recorder.inputTypes).containsExactly(InputType.JSON);
    }

    @Test
    void rejectsFilesThatAreNeitherTextNorJson() {
        final EvaluationService service = service(new NoOpScenarioRecorder());

        assertThatThrownBy(() -> service.evaluateFile("input.csv", stream(TEXT)))
                .isInstanceOf(ScenarioParseException.class)
                .hasMessage("Unsupported file 'input.csv': upload a .txt or .json file");
    }

    private static EvaluationService service(final ScenarioRecorder recorder) {
        return new EvaluationService(new TextScenarioParser(InputLimits.DEFAULT),
                new JsonScenarioParser(InputLimits.DEFAULT), new TradingService(new FlutTradingOptimizer(TradingRules.SPECIFICATION)), recorder);
    }

    private static InputStream stream(final String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    /** Pretends to save: returns ids 100, 101, ... */
    private static final class CapturingRecorder implements ScenarioRecorder {

        private final List<InputType> inputTypes = new ArrayList<>();

        @Override
        public List<Long> record(final List<Scenario> scenarios, final InputType inputType) {
            inputTypes.add(inputType);
            return LongStream.range(100, 100 + scenarios.size()).boxed().toList();
        }
    }
}
