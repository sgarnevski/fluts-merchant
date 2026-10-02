package com.fluts.trading;

import com.fluts.domain.Scenario;
import com.fluts.domain.TradingResult;

/**
 * A scenario together with its result.
 *
 * @param scenario the evaluated scenario
 * @param result   its maximum profit and the numbers of fluts that reach it
 */
public record Evaluation(Scenario scenario, TradingResult result) {
}
