package com.fluts.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Finds the maximum profit for a scenario and every number of fluts that reaches it.
 *
 * <p>Piles are independent: the best total profit is the sum of each pile's best profit. For one
 * pile, buying the top {@code k} boxes earns {@code sum(sellingPrice - price)} over those boxes,
 * so every {@code k} with the maximal running profit is optimal. The optimal total counts are all
 * sums of one optimal {@code k} per pile. Only the smallest ones are reported (how many: the
 * {@link TradingRules}), and keeping only the smallest partial sums after each pile is enough to get them.
 *
 * <p>Profits are computed as {@code long}: prices are unbounded, so sums could overflow {@code int}.
 * Written as plain loops on purpose: this is the one place where the algorithm should read like
 * its description.
 */
public final class FlutTradingOptimizer {

    private final TradingRules rules;

    public FlutTradingOptimizer(final TradingRules rules) {
        this.rules = rules;
    }

    public TradingResult optimize(final Scenario scenario) {
        long maxProfit = 0;
        NavigableSet<Integer> flutCounts = new TreeSet<>(List.of(0));
        for (final Schuur schuur : scenario.schuurs()) {
            final SchuurResult pile = optimizeSchuur(schuur);
            maxProfit += pile.maxProfit();
            flutCounts = smallestSums(flutCounts, pile.flutCounts());
        }
        return new TradingResult(maxProfit, List.copyOf(flutCounts));
    }

    /**
     * The best profit at one schuur and every number of boxes from the top that earns it: walk the
     * pile once, keeping the running profit of the top {@code k} boxes.
     */
    public SchuurResult optimizeSchuur(final Schuur schuur) {
        long profit = 0;
        long best = 0;
        final List<Integer> bestCounts = new ArrayList<>();
        bestCounts.add(0);
        int k = 0;
        for (final int price : schuur.boxPrices()) {
            k++;
            profit += rules.sellingPrice() - price;
            if (profit > best) {
                best = profit;
                bestCounts.clear();
                bestCounts.add(k);
            } else if (profit == best && bestCounts.size() < rules.maxReportedCounts()) {
                bestCounts.add(k);
            }
        }
        return new SchuurResult(best, bestCounts);
    }

    /** The smallest distinct sums {@code a + b} with a from left, b from right, as many as the rules report. */
    private NavigableSet<Integer> smallestSums(final NavigableSet<Integer> left, final List<Integer> right) {
        final NavigableSet<Integer> sums = new TreeSet<>();
        for (final int a : left) {
            for (final int b : right) {
                sums.add(a + b);
            }
        }
        while (sums.size() > rules.maxReportedCounts()) {
            sums.pollLast();
        }
        return sums;
    }
}
