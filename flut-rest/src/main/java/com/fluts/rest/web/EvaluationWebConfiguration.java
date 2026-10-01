package com.fluts.rest.web;

import com.fluts.domain.FlutTradingOptimizer;
import com.fluts.io.JsonScenarioParser;
import com.fluts.io.TextResultFormatter;
import com.fluts.io.TextScenarioParser;
import com.fluts.rest.evaluation.EvaluationService;
import com.fluts.rest.evaluation.ScenarioRecorder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The complete evaluation web layer: {@code POST /api/evaluations}, error handling and OpenAPI.
 * An app imports this configuration and provides one {@link ScenarioRecorder} bean.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LimitsProperties.class)
@Import({EvaluationController.class, ApiExceptionHandler.class})
public class EvaluationWebConfiguration implements WebMvcConfigurer {

    private final TextResultFormatter textResultFormatter = new TextResultFormatter();

    @Bean
    TextScenarioParser textScenarioParser(final LimitsProperties limits) {
        return new TextScenarioParser(limits.toInputLimits());
    }

    @Bean
    JsonScenarioParser jsonScenarioParser(final LimitsProperties limits) {
        return new JsonScenarioParser(limits.toInputLimits());
    }

    @Bean
    FlutTradingOptimizer flutTradingOptimizer() {
        return new FlutTradingOptimizer();
    }

    @Bean
    EvaluationService evaluationService(final TextScenarioParser textScenarioParser,
            final JsonScenarioParser jsonScenarioParser, final FlutTradingOptimizer flutTradingOptimizer,
            final ScenarioRecorder scenarioRecorder) {
        return new EvaluationService(textScenarioParser, jsonScenarioParser, flutTradingOptimizer, scenarioRecorder);
    }

    @Bean
    OpenAPI flutOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trading of Fluts API")
                        .version("1.0")
                        .description("Maximum profit and the numbers of fluts to buy, for piles bought from the top."))
                .addServersItem(new Server().url("/"));
    }

    /**
     * Both {@code POST /api/evaluations} handlers share one OpenAPI operation, and springdoc drops the
     * JSON example while merging them; this puts it back.
     */
    @Bean
    OpenApiCustomizer evaluationJsonExample() {
        return openApi -> openApi.getPaths().get("/api/evaluations").getPost().getRequestBody().getContent()
                .get(MediaType.APPLICATION_JSON_VALUE)
                .setExample(OpenApiExamples.JSON_INPUT);
    }

    @Bean
    RequiredRecordComponentsConverter requiredRecordComponentsConverter() {
        return new RequiredRecordComponentsConverter();
    }

    @Override
    public void configureMessageConverters(final HttpMessageConverters.ServerBuilder builder) {
        builder.addCustomConverter(new EvaluationTextConverter(textResultFormatter));
    }
}
