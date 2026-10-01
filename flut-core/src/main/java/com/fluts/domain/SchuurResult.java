package com.fluts.domain;

import java.util.List;

/**
 * The best a merchant can do at one schuur.
 *
 * @param maxProfit  the profit of buying the best number of boxes from the top
 * @param flutCounts every number of boxes (from the top) that earns {@code maxProfit}, ascending, at most
 *                   the {@value FlutTradingOptimizer#MAX_REPORTED_COUNTS} smallest; {@code 0} when
 *                   buying nothing is (one of) the best
 */
public record SchuurResult(long maxProfit, List<Integer> flutCounts) {

    public SchuurResult {
        flutCounts = List.copyOf(flutCounts);
    }
}
