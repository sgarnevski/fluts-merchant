package com.fluts.rest.web;

import com.fluts.domain.TradingResult;
import com.fluts.io.TextResultFormatter;
import com.fluts.rest.evaluation.EvaluationResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

/** Writes an {@link EvaluationResponse} as {@code text/plain} in the original output format of the specification. */
public final class EvaluationTextConverter extends AbstractHttpMessageConverter<EvaluationResponse> {

    private final TextResultFormatter formatter;

    public EvaluationTextConverter(final TextResultFormatter formatter) {
        super(StandardCharsets.UTF_8, MediaType.TEXT_PLAIN);
        this.formatter = formatter;
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return EvaluationResponse.class.equals(clazz);
    }

    @Override
    protected boolean canRead(final MediaType mediaType) {
        return false;
    }

    @Override
    protected EvaluationResponse readInternal(final Class<? extends EvaluationResponse> clazz,
            final HttpInputMessage inputMessage) {
        throw new HttpMessageNotReadableException("Evaluation results are write-only", inputMessage);
    }

    @Override
    protected void writeInternal(final EvaluationResponse response, final HttpOutputMessage outputMessage)
            throws IOException {
        final String text = formatter.format(response.results().stream()
                .map(result -> new TradingResult(result.maxProfit(), result.flutCounts()))
                .toList());
        outputMessage.getBody().write(text.getBytes(StandardCharsets.UTF_8));
    }
}
