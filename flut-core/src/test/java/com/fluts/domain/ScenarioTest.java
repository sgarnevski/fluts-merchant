package com.fluts.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScenarioTest {

    private static final Schuur PILE = new Schuur(List.of(1));

    @Test
    void keepsItsNameAndPiles() {
        final Scenario scenario = new Scenario("Example", List.of(PILE));

        assertThat(scenario.name()).isEqualTo("Example");
        assertThat(scenario.schuurs()).containsExactly(PILE);
    }

    @Test
    void cannotBeChangedFromOutside() {
        final List<Schuur> schuurs = new ArrayList<>(List.of(PILE));
        final Scenario scenario = new Scenario("copy", schuurs);

        schuurs.add(new Schuur(List.of(2)));

        assertThat(scenario.schuurs()).hasSize(1);
    }

    @Test
    void rejectsABlankName() {
        assertThatThrownBy(() -> new Scenario(" ", List.of(PILE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Scenario name must not be blank");
    }

    @Test
    void rejectsAMissingName() {
        assertThatThrownBy(() -> new Scenario(null, List.of(PILE))).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsAScenarioWithoutPiles() {
        assertThatThrownBy(() -> new Scenario("empty", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A scenario needs at least one schuur");
    }

    @Test
    void rejectsMissingPiles() {
        assertThatThrownBy(() -> new Scenario("none", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsAMissingPile() {
        assertThatThrownBy(() -> new Scenario("gap", Arrays.asList(PILE, null))).isInstanceOf(NullPointerException.class);
    }
}
