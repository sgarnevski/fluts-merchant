package com.fluts.cli;

import com.fluts.io.InputLimits;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Input limits, configurable as {@code flut.limits.*}. */
@ConfigurationProperties("flut.limits")
public record LimitsProperties(@DefaultValue("1000") int maxSchuursPerScenario,
        @DefaultValue("10000") int maxBoxesPerSchuur) {

    public InputLimits toInputLimits() {
        return new InputLimits(maxSchuursPerScenario, maxBoxesPerSchuur);
    }
}
