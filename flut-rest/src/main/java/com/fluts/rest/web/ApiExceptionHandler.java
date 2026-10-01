package com.fluts.rest.web;

import com.fluts.io.ScenarioParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Errors as RFC 9457 problem details. Framework errors (missing part, upload too large, wrong
 * media type, ...) are handled by the {@link ResponseEntityExceptionHandler} base class.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail handleInvalidInput(final ScenarioParseException exception) {
        final ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.detail());
        problem.setTitle("Invalid input");
        exception.line().ifPresent(line -> problem.setProperty("line", line));
        return problem;
    }
}
