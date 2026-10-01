package com.fluts.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Finds the maximum profit for a scenario and every number of fluts that reaches it.
 *
 * <p>Piles are independent: the best total profit is the sum of each pile's best profit. For one
 * pile, buying the top {@code k} boxes earns {@code sum(SELLING_PRICE - price)} over those boxes,
 * so every {@code k} with the maximal running profit is optimal. The optimal total counts are all
 * sums of one optimal {@code k} per pile. Only the {@value #MAX_REPORTED_COUNTS} smallest are
 * reported, and keeping only the smallest partial sums after each pile is enough to get them.
 *
 * <p>Profits are computed as {@code long}: prices are unbounded, so sums could overflow {@code int}.
 * Written as plain loops on purpose: this is the one place where the algorithm should read like
 * its description.
 */
public final class FlutTradingOptimizer {

    /** Price one flut sells for in Holland, in florins. */
    public static final int SELLING_PRICE = 10;

    /** How many of the smallest optimal flut counts are reported. */
    public static final int MAX_REPORTED_COUNTS = 10;

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
    public static SchuurResult optimizeSchuur(final Schuur schuur) {
        long profit = 0;
        long best = 0;
        final List<Integer> bestCounts = new ArrayList<>();
        bestCounts.add(0);
        int k = 0;
        for (final int price : schuur.boxPrices()) {
            k++;
            profit += SELLING_PRICE - price;
            if (profit > best) {
                best = profit;
                bestCounts.clear();
                bestCounts.add(k);
            } else if (profit == best && bestCounts.size() < MAX_REPORTED_COUNTS) {
                bestCounts.add(k);
            }
        }
        return new SchuurResult(best, bestCounts);
    }

    /** The {@value #MAX_REPORTED_COUNTS} smallest distinct sums {@code a + b} with a from left, b from right. */
    private static NavigableSet<Integer> smallestSums(final NavigableSet<Integer> left, final List<Integer> right) {
        final NavigableSet<Integer> sums = new TreeSet<>();
        for (final int a : left) {
            for (final int b : right) {
                sums.add(a + b);
            }
        }
        while (sums.size() > MAX_REPORTED_COUNTS) {
            sums.pollLast();
        }
        return sums;
    }
}
