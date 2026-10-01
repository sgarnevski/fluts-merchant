package com.fluts.cli;

import com.fluts.domain.FlutTradingOptimizer;
import com.fluts.io.TextResultFormatter;
import com.fluts.io.TextScenarioParser;
import java.nio.charset.StandardCharsets;
import java.io.PrintStream;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the plain-Java parser, optimizer and formatter, and the process streams. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LimitsProperties.class)
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
    FlutTradingOptimizer flutTradingOptimizer() {
        return new FlutTradingOptimizer();
    }

    @Bean
    TextResultFormatter textResultFormatter() {
        return new TextResultFormatter();
    }
}
