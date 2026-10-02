package com.fluts.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The two result records are plain values: they keep their own copy of the counts. */
class ResultTest {

    @Test
    void tradingResultCannotBeChangedFromOutside() {
        final List<Integer> counts = new ArrayList<>(List.of(1));
        final TradingResult result = new TradingResult(5, counts);

        counts.add(2);

        assertThat(result.flutCounts()).containsExactly(1);
    }

    @Test
    void schuurResultCannotBeChangedFromOutside() {
        final List<Integer> counts = new ArrayList<>(List.of(1));
        final SchuurResult result = new SchuurResult(5, counts);

        counts.add(2);

        assertThat(result.flutCounts()).containsExactly(1);
    }

    @Test
    void tradingResultRejectsMissingCounts() {
        assertThatThrownBy(() -> new TradingResult(0, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void schuurResultRejectsMissingCounts() {
        assertThatThrownBy(() -> new SchuurResult(0, null)).isInstanceOf(NullPointerException.class);
    }
}
