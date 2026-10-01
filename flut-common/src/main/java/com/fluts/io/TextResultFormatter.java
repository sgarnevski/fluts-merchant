package com.fluts.io;

import com.fluts.domain.TradingResult;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Formats results in the original output format of the specification: per scenario its case
 * number, the maximum profit and the flut counts, with a blank line between scenarios.
 * Lines always end with {@code \n}.
 */
public final class TextResultFormatter {

    public String format(final List<TradingResult> results) {
        return IntStream.range(0, results.size())
                .mapToObj(index -> formatOne(index + 1, results.get(index)))
                .collect(Collectors.joining("\n"));
    }

    /** One block, with the given case number (the specification counts the cases of one input from 1). */
    public String format(final int caseNumber, final TradingResult result) {
        return formatOne(caseNumber, result);
    }

    private static String formatOne(final int caseNumber, final TradingResult result) {
        final String counts = result.flutCounts().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
        return "schuurs " + caseNumber + "\n"
                + "Maximum profit is " + result.maxProfit() + ".\n"
                + "Number of fluts to buy: " + counts + "\n";
    }
}
