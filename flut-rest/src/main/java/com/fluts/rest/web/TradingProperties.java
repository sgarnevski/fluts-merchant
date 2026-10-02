package com.fluts.rest.web;

import com.fluts.domain.TradingRules;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The rules of the trade, configurable as {@code flut.trading.*}; the defaults are the specification's.
 *
 * @param sellingPrice      what one flut sells for in Holland, in florins
 * @param maxReportedCounts how many of the smallest optimal numbers of fluts are reported
 */
@ConfigurationProperties("flut.trading")
public record TradingProperties(@DefaultValue("10") int sellingPrice, @DefaultValue("10") int maxReportedCounts) {

    public TradingRules toTradingRules() {
        return new TradingRules(sellingPrice, maxReportedCounts);
    }
}
