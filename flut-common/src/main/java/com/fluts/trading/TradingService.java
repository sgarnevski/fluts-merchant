package com.fluts.trading;

import com.fluts.domain.FlutTradingOptimizer;
import com.fluts.domain.Scenario;
import com.fluts.io.ScenarioParser;
import java.io.InputStream;
import java.util.List;

/**
 * The one evaluation use case every app shares: read scenarios from an input and optimize each of
 * them. The console app and the REST API both go through it; the algorithm itself stays in
 * {@link FlutTradingOptimizer}.
 */
public final class TradingService {

    private final FlutTradingOptimizer optimizer;

    public TradingService(final FlutTradingOptimizer optimizer) {
        this.optimizer = optimizer;
    }

    /**
     * Parses the input and evaluates every scenario in it, in input order.
     *
     * @throws com.fluts.io.ScenarioParseException if the input is not valid for the parser's format
     * @throws java.io.UncheckedIOException       if the input cannot be read
     */
    public List<Evaluation> evaluate(final ScenarioParser parser, final InputStream input) {
        return evaluate(parser.parse(input));
    }

    /** Evaluates the scenarios, in the given order. */
    public List<Evaluation> evaluate(final List<Scenario> scenarios) {
        return scenarios.stream()
                .map(scenario -> new Evaluation(scenario, optimizer.optimize(scenario)))
                .toList();
    }
}
