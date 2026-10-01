package com.fluts.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InputLimitsTest {

    @Test
    void defaultsMatchThePlan() {
        assertThat(InputLimits.DEFAULT).isEqualTo(new InputLimits(1_000, 10_000));
    }

    @Test
    void rejectsNonPositiveSchuurLimit() {
        assertThatThrownBy(() -> new InputLimits(0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limits must be positive");
    }

    @Test
    void rejectsNonPositiveBoxLimit() {
        assertThatThrownBy(() -> new InputLimits(1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limits must be positive");
    }
}
