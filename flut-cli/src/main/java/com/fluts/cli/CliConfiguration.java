package com.fluts.cli;

import com.fluts.domain.FlutTradingOptimizer;
import com.fluts.io.TextResultFormatter;
import com.fluts.io.TextScenarioParser;
import com.fluts.trading.TradingService;
import java.nio.charset.StandardCharsets;
import java.io.PrintStream;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the plain-Java parser, trading service and formatter, and the process streams. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({LimitsProperties.class, TradingProperties.class})
class CliConfiguration {

    @Bean
    ConsoleStreams consoleStreams() {
        return new ConsoleStreams(System.in,
                new PrintStream(System.out, true, StandardCharsets.UTF_8),
                new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }

    @Bean
    TextScenarioParser textScenarioParser(final LimitsProperties limits) {
        return new TextScenarioParser(limits.toInputLimits());
    }

    @Bean
    TradingService tradingService(final TradingProperties trading) {
        return new TradingService(new FlutTradingOptimizer(trading.toTradingRules()));
    }

    @Bean
    TextResultFormatter textResultFormatter() {
        return new TextResultFormatter();
    }
}
