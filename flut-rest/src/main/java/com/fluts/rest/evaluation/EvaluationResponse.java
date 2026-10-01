package com.fluts.rest.evaluation;

import java.util.List;

/** Results for all scenarios of one request, in input order. */
public record EvaluationResponse(List<EvaluationResult> results) {

    public EvaluationResponse {
        results = List.copyOf(results);
    }
}
