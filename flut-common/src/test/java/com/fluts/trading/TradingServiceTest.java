package com.fluts.trading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fluts.domain.FlutTradingOptimizer;
import com.fluts.domain.Scenario;
import com.fluts.domain.Schuur;
import com.fluts.domain.TradingResult;
import com.fluts.domain.TradingRules;
import com.fluts.io.InputLimits;
import com.fluts.io.JsonScenarioParser;
import com.fluts.io.ScenarioParseException;
import com.fluts.io.TextScenarioParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class TradingServiceTest {

    private final TradingService service = new TradingService(new FlutTradingOptimizer(TradingRules.SPECIFICATION));

    @Test
    void evaluatesEveryScenarioOfAnInputInOrder() throws IOException {
        try (InputStream input = Files.newInputStream(Path.of("../demo/input.txt"))) {
            final List<Evaluation> evaluations = service.evaluate(new TextScenarioParser(InputLimits.DEFAULT), input);

            assertThat(evaluations).extracting(Evaluation::result).containsExactly(
                    new TradingResult(8, List.of(4)),
                    new TradingResult(40, List.of(6, 7, 8, 9, 10, 12, 13)));
            assertThat(evaluations).extracting(evaluation -> evaluation.scenario().name())
                    .containsExactly("Scenario 1", "Scenario 2");
        }
    }

    @Test
    void evaluatesScenariosAsGiven() {
        final Scenario scenario = new Scenario("One box", List.of(new Schuur(List.of(1))));

        assertThat(service.evaluate(List.of(scenario))).containsExactly(new Evaluation(scenario, new TradingResult(9, List.of(1))));
    }

    @Test
    void appliesTheRulesItWasGiven() {
        final TradingService dearMarket = new TradingService(new FlutTradingOptimizer(new TradingRules(15, 10)));
        final Scenario scenario = new Scenario("Dear", List.of(new Schuur(List.of(12))));

        assertThat(dearMarket.evaluate(List.of(scenario)).getFirst().result()).isEqualTo(new TradingResult(3, List.of(1)));
    }

    @Test
    void evaluatesNothingWhenThereIsNothingToEvaluate() {
        assertThat(service.evaluate(List.of())).isEmpty();
        assertThat(service.evaluate(new TextScenarioParser(InputLimits.DEFAULT), stream("0\n"))).isEmpty();
    }

    @Test
    void passesInvalidInputOnAsAParseError() {
        assertThatThrownBy(() -> service.evaluate(new JsonScenarioParser(InputLimits.DEFAULT), stream("{\"scenarios\": []}")))
                .isInstanceOf(ScenarioParseException.class)
                .hasMessage("No scenarios given");
    }

    private static InputStream stream(final String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
