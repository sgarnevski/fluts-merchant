package com.fluts.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainValidationTest {

    @Test
    void schuurRejectsNonPositivePrices() {
        assertThatThrownBy(() -> new Schuur(List.of(3, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Box prices must be positive, got 0");
    }

    @Test
    void schuurRejectsMissingPrices() {
        assertThatThrownBy(() -> new Schuur(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void schuurKeepsAnImmutableCopy() {
        final List<Integer> prices = new ArrayList<>(List.of(1, 2));
        final Schuur schuur = new Schuur(prices);

        prices.add(3);

        assertThat(schuur.boxPrices()).containsExactly(1, 2);
        assertThatThrownBy(() -> schuur.boxPrices().add(4)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void scenarioRejectsBlankName() {
        assertThatThrownBy(() -> new Scenario(" ", List.of(new Schuur(List.of(1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Scenario name must not be blank");
    }

    @Test
    void scenarioRejectsMissingName() {
        assertThatThrownBy(() -> new Scenario(null, List.of(new Schuur(List.of(1)))))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void scenarioRequiresAtLeastOneSchuur() {
        assertThatThrownBy(() -> new Scenario("empty", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A scenario needs at least one schuur");
    }

    @Test
    void scenarioKeepsAnImmutableCopy() {
        final List<Schuur> schuurs = new ArrayList<>(List.of(new Schuur(List.of(1))));
        final Scenario scenario = new Scenario("copy", schuurs);

        schuurs.add(new Schuur(List.of(2)));

        assertThat(scenario.schuurs()).hasSize(1);
    }

    @Test
    void schuurResultKeepsAnImmutableCopy() {
        final List<Integer> counts = new ArrayList<>(List.of(1));
        final SchuurResult result = new SchuurResult(5, counts);

        counts.add(2);

        assertThat(result.flutCounts()).containsExactly(1);
    }

    @Test
    void tradingResultKeepsAnImmutableCopy() {
        final List<Integer> counts = new ArrayList<>(List.of(1));
        final TradingResult result = new TradingResult(5, counts);

        counts.add(2);

        assertThat(result.flutCounts()).containsExactly(1);
    }
}
