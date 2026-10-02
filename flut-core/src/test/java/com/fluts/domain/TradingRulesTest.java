package com.fluts.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TradingRulesTest {

    @Test
    void specificationSellsAFlutForTenFlorinsAndReportsTenCounts() {
        assertThat(TradingRules.SPECIFICATION).isEqualTo(new TradingRules(10, 10));
    }

    @Test
    void rejectsAFreeOrNegativeSellingPrice() {
        assertThatThrownBy(() -> new TradingRules(0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Selling price must be positive, got 0");
        assertThatThrownBy(() -> new TradingRules(-1, 10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsReportingNoCounts() {
        assertThatThrownBy(() -> new TradingRules(10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Number of reported counts must be positive, got 0");
    }
}
