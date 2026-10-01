package com.fluts.domain;

import java.util.List;

/**
 * Outcome of optimizing one scenario.
 *
 * @param maxProfit  the maximum profit in florins
 * @param flutCounts every number of fluts that reaches {@code maxProfit}, ascending, at most the
 *                   {@value FlutTradingOptimizer#MAX_REPORTED_COUNTS} smallest
 */
public record TradingResult(long maxProfit, List<Integer> flutCounts) {

    public TradingResult {
        flutCounts = List.copyOf(flutCounts);
    }
}
