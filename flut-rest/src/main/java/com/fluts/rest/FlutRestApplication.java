package com.fluts.rest;

import com.fluts.rest.evaluation.NoOpScenarioRecorder;
import com.fluts.rest.evaluation.ScenarioRecorder;
import com.fluts.rest.web.EvaluationWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Stateless REST app: the evaluation API without a database. Wired explicitly
 * ({@link EvaluationWebConfiguration} import, no component scan); the {@link ScenarioRecorder}
 * is the hook an app with a database would replace. Here nothing is saved.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(EvaluationWebConfiguration.class)
public class FlutRestApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FlutRestApplication.class, args);
    }

    @Bean
    ScenarioRecorder scenarioRecorder() {
        return new NoOpScenarioRecorder();
    }
}
