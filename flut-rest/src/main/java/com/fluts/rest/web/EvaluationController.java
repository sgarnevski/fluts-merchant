package com.fluts.rest.web;

import com.fluts.io.ScenariosInput;
import com.fluts.rest.evaluation.EvaluationResponse;
import com.fluts.rest.evaluation.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@code POST /api/evaluations}: solves the scenarios of a JSON body or an uploaded file.
 * Responds with JSON, or with the original text output for {@code Accept: text/plain}.
 */
@RestController
@RequestMapping(path = "/api/evaluations", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
@Tag(name = "Evaluations", description = "Solve scenarios: maximum profit and the numbers of fluts to buy")
@ApiResponse(responseCode = "200", description = "Results in input order",
        content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EvaluationResponse.class)),
            @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = OpenApiExamples.TEXT_OUTPUT))})
@ApiResponse(responseCode = "400", description = "Invalid input (with the line number for text input)",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class),
                examples = @ExampleObject(value = OpenApiExamples.PROBLEM)))
@ApiResponse(responseCode = "413", description = "Input larger than the configured limit",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class)))
public class EvaluationController {

    private static final String OPERATION_DESCRIPTION = """
            Send the scenarios either as a JSON body (normalized format) or as an uploaded file \
            (multipart part `file`): `.txt` in the original format of the specification, or `.json` \
            in the normalized format. Send `Accept: text/plain` to get the original output format.""";

    private final EvaluationService evaluationService;
    private final LimitsProperties limits;

    public EvaluationController(final EvaluationService evaluationService, final LimitsProperties limits) {
        this.evaluationService = evaluationService;
        this.limits = limits;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Evaluate scenarios", description = OPERATION_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ScenariosInput.class))))
    public EvaluationResponse evaluateJson(@Parameter(hidden = true) final InputStream body) throws IOException {
        return evaluationService.evaluateJson(limited(body));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Evaluate scenarios", description = OPERATION_DESCRIPTION)
    public EvaluationResponse evaluateFile(
            @Parameter(description = "A .txt or .json file") @RequestPart("file") final MultipartFile file)
            throws IOException {
        try (InputStream content = file.getInputStream()) {
            return evaluationService.evaluateFile(Objects.requireNonNullElse(file.getOriginalFilename(), ""), content);
        }
    }

    /** Reads at most the configured body size, so a huge body cannot exhaust memory. */
    private InputStream limited(final InputStream body) throws IOException {
        final long maxBytes = limits.maxBodySize().toBytes();
        final byte[] bytes = body.readNBytes(Math.toIntExact(maxBytes + 1));
        if (bytes.length > maxBytes) {
            throw new ErrorResponseException(HttpStatus.CONTENT_TOO_LARGE,
                    ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE,
                            "Request body is larger than " + limits.maxBodySize()), null);
        }
        return new ByteArrayInputStream(bytes);
    }
}
