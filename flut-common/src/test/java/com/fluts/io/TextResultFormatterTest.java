package com.fluts.io;

import static org.assertj.core.api.Assertions.assertThat;

import com.fluts.domain.TradingResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class TextResultFormatterTest {

    private final TextResultFormatter formatter = new TextResultFormatter();

    @Test
    void formatsExactlyLikeTheSpecification() throws IOException {
        final String expected = Files.readString(Path.of("../demo/output.txt"));

        final String output = formatter.format(List.of(
                new TradingResult(8, List.of(4)),
                new TradingResult(40, List.of(6, 7, 8, 9, 10, 12, 13))));

        assertThat(output).isEqualTo(expected);
    }

    @Test
    void formatsASingleScenarioWithoutTrailingBlankLine() {
        assertThat(formatter.format(List.of(new TradingResult(0, List.of(0)))))
                .isEqualTo("schuurs 1\nMaximum profit is 0.\nNumber of fluts to buy: 0\n");
    }

    @Test
    void formatsOneBlockWithAGivenCaseNumber() {
        assertThat(formatter.format(3, new TradingResult(630, List.of(170))))
                .isEqualTo("schuurs 3\nMaximum profit is 630.\nNumber of fluts to buy: 170\n");
    }

    @Test
    void formatsNothingForNoScenarios() {
        assertThat(formatter.format(List.of())).isEmpty();
    }
}
