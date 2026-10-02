package com.fluts.domain;

/**
 * The rules of the trade the optimizer works with.
 *
 * @param sellingPrice      what one flut sells for in Holland, in florins
 * @param maxReportedCounts how many of the smallest optimal numbers of fluts are reported
 */
public record TradingRules(int sellingPrice, int maxReportedCounts) {

    /** The rules of the specification: a flut sells for 10 florins, the 10 smallest counts are reported. */
    public static final TradingRules SPECIFICATION = new TradingRules(10, 10);

    public TradingRules {
        if (sellingPrice <= 0) {
            throw new IllegalArgumentException("Selling price must be positive, got " + sellingPrice);
        }
        if (maxReportedCounts <= 0) {
            throw new IllegalArgumentException("Number of reported counts must be positive, got " + maxReportedCounts);
        }
    }
}
