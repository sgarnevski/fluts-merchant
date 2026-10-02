package com.fluts.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InputLimitsTest {

    @Test
    void allowsAThousandSchuursOfTenThousandBoxesByDefault() {
        assertThat(InputLimits.DEFAULT).isEqualTo(new InputLimits(1_000, 10_000));
    }

    @Test
    void rejectsAZeroSchuurLimit() {
        assertThatThrownBy(() -> new InputLimits(0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limits must be positive");
    }

    @Test
    void rejectsAZeroBoxLimit() {
        assertThatThrownBy(() -> new InputLimits(1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limits must be positive");
    }

    @Test
    void rejectsNegativeLimits() {
        assertThatThrownBy(() -> new InputLimits(-1, 5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InputLimits(5, -1)).isInstanceOf(IllegalArgumentException.class);
    }
}
