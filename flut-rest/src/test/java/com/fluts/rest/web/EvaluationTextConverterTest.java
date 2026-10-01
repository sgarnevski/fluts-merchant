package com.fluts.rest.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fluts.io.TextResultFormatter;
import com.fluts.rest.evaluation.EvaluationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

class EvaluationTextConverterTest {

    private final EvaluationTextConverter converter = new EvaluationTextConverter(new TextResultFormatter());

    @Test
    void writesOnlyEvaluationResponsesAsText() {
        assertThat(converter.canWrite(EvaluationResponse.class, MediaType.TEXT_PLAIN)).isTrue();
        assertThat(converter.canWrite(String.class, MediaType.TEXT_PLAIN)).isFalse();
    }

    @Test
    void neverReads() {
        assertThat(converter.canRead(EvaluationResponse.class, MediaType.TEXT_PLAIN)).isFalse();
        assertThatThrownBy(() -> converter.read(EvaluationResponse.class, new MockHttpInputMessage(new byte[0])))
                .isInstanceOf(HttpMessageNotReadableException.class);
    }
}
