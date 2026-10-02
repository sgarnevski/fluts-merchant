package com.fluts.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SchuurTest {

    @Test
    void keepsThePricesTopToBottom() {
        assertThat(new Schuur(List.of(12, 3, 10)).boxPrices()).containsExactly(12, 3, 10);
    }

    @Test
    void mayBeEmpty() {
        assertThat(new Schuur(List.of()).boxPrices()).isEmpty();
    }

    @Test
    void cannotBeChangedFromOutside() {
        final List<Integer> prices = new ArrayList<>(List.of(1, 2));
        final Schuur schuur = new Schuur(prices);

        prices.add(3);

        assertThat(schuur.boxPrices()).containsExactly(1, 2);
        assertThatThrownBy(() -> schuur.boxPrices().add(4)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsAFreeBox() {
        assertThatThrownBy(() -> new Schuur(List.of(3, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Box prices must be positive, got 0");
    }

    @Test
    void rejectsANegativePrice() {
        assertThatThrownBy(() -> new Schuur(List.of(-5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Box prices must be positive, got -5");
    }

    @Test
    void rejectsAMissingPrice() {
        assertThatThrownBy(() -> new Schuur(Arrays.asList(1, null))).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsMissingPrices() {
        assertThatThrownBy(() -> new Schuur(null)).isInstanceOf(NullPointerException.class);
    }
}
