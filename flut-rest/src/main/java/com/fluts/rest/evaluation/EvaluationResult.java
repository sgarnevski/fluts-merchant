package com.fluts.rest.evaluation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Result for one scenario.
 *
 * @param id         id of the saved scenario; absent when the scenario was not saved (always, in this app)
 * @param scenario   scenario name
 * @param maxProfit  maximum profit in florins
 * @param flutCounts every number of fluts reaching the maximum profit, ascending, at most the 10 smallest
 */
@Schema(description = "Result for one scenario")
public record EvaluationResult(
        @Schema(description = "Id of the saved scenario; present only when the scenario was saved", example = "17")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Nullable Long id,
        @Schema(example = "Example 2") String scenario,
        @Schema(example = "40") long maxProfit,
        @Schema(example = "[6, 7, 8, 9, 10, 12, 13]") List<Integer> flutCounts) {

    public EvaluationResult {
        flutCounts = List.copyOf(flutCounts);
    }
}
