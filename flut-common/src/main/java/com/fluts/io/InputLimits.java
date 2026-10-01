package com.fluts.io;

/**
 * Upper bounds for parsed input, so a huge or hostile input fails fast instead of exhausting memory.
 *
 * @param maxSchuursPerScenario maximum number of schuurs in one scenario
 * @param maxBoxesPerSchuur     maximum number of boxes in one schuur
 */
public record InputLimits(int maxSchuursPerScenario, int maxBoxesPerSchuur) {

    public static final int DEFAULT_MAX_SCHUURS_PER_SCENARIO = 1_000;
    public static final int DEFAULT_MAX_BOXES_PER_SCHUUR = 10_000;

    public static final InputLimits DEFAULT =
            new InputLimits(DEFAULT_MAX_SCHUURS_PER_SCENARIO, DEFAULT_MAX_BOXES_PER_SCHUUR);

    public InputLimits {
        if (maxSchuursPerScenario <= 0 || maxBoxesPerSchuur <= 0) {
            throw new IllegalArgumentException("Limits must be positive");
        }
    }
}
