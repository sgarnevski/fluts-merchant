package com.fluts.io;

import java.io.Serial;
import java.util.OptionalInt;

/** Invalid input. Carries the 1-based line number when the position is known. */
public final class ScenarioParseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int UNKNOWN_LINE = 0;

    private final String detail;
    private final int line;

    private ScenarioParseException(final String detail, final int line, final Throwable cause) {
        super(line == UNKNOWN_LINE ? detail : "Line " + line + ": " + detail, cause);
        this.detail = detail;
        this.line = line;
    }

    public static ScenarioParseException atLine(final int line, final String detail) {
        return new ScenarioParseException(detail, line, null);
    }

    public static ScenarioParseException atLine(final int line, final String detail, final Throwable cause) {
        return new ScenarioParseException(detail, line, cause);
    }

    public static ScenarioParseException of(final String detail) {
        return new ScenarioParseException(detail, UNKNOWN_LINE, null);
    }

    /** What is wrong, without the line prefix. */
    public String detail() {
        return detail;
    }

    public OptionalInt line() {
        return line == UNKNOWN_LINE ? OptionalInt.empty() : OptionalInt.of(line);
    }
}
