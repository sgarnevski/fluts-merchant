package com.fluts.rest.web;

import com.fluts.io.InputLimits;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

/**
 * Input limits, configurable as {@code flut.limits.*}. Uploads are limited separately by
 * {@code spring.servlet.multipart.*}.
 *
 * @param maxSchuursPerScenario maximum number of schuurs in one scenario
 * @param maxBoxesPerSchuur     maximum number of boxes in one schuur
 * @param maxBodySize           maximum size of a JSON request body
 */
@ConfigurationProperties("flut.limits")
public record LimitsProperties(@DefaultValue("1000") int maxSchuursPerScenario,
        @DefaultValue("10000") int maxBoxesPerSchuur,
        @DefaultValue("1MB") DataSize maxBodySize) {

    public InputLimits toInputLimits() {
        return new InputLimits(maxSchuursPerScenario, maxBoxesPerSchuur);
    }
}
