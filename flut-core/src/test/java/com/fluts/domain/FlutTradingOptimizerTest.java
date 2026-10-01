package com.fluts.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FlutTradingOptimizerTest {

    private final FlutTradingOptimizer optimizer = new FlutTradingOptimizer();

    static Stream<Arguments> scenarios() {
        return Stream.of(
                Arguments.of("PDF example 1",
                        List.of(pile(12, 3, 10, 7, 16, 5)),
                        8L, List.of(4)),
                Arguments.of("PDF example 2",
                        List.of(pile(7, 3, 11, 9, 10), pile(1, 2, 3, 4, 10, 16, 10, 4, 16)),
                        40L, List.of(6, 7, 8, 9, 10, 12, 13)),
                Arguments.of("all prices above selling price: buy nothing",
                        List.of(pile(11, 12), pile(20)),
                        0L, List.of(0)),
                Arguments.of("ties inside one pile",
                        List.of(pile(5, 15, 5)),
                        5L, List.of(1, 3)),
                Arguments.of("price exactly 10 gives zero-profit ties, including zero",
                        List.of(pile(10, 10)),
                        0L, List.of(0, 1, 2)),
                Arguments.of("empty pile contributes nothing",
                        List.of(pile(), pile(4)),
                        6L, List.of(1)),
                Arguments.of("single profitable box",
                        List.of(pile(1)),
                        9L, List.of(1)),
                Arguments.of("single unprofitable box",
                        List.of(pile(11)),
                        0L, List.of(0)),
                Arguments.of("more than ten counts in one pile: only the 10 smallest",
                        List.of(pile(tens(12))),
                        0L, List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
                Arguments.of("more than ten counts across piles: only the 10 smallest",
                        List.of(pile(9, 10, 10, 10, 10, 10, 10), pile(9, 10, 10, 10, 10, 10, 10)),
                        2L, List.of(2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
                Arguments.of("huge prices do not overflow",
                        List.of(pile(Integer.MAX_VALUE, Integer.MAX_VALUE, 1)),
                        0L, List.of(0)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void findsMaximumProfitAndSmallestFlutCounts(final String description, final List<Schuur> schuurs,
            final long expectedProfit, final List<Integer> expectedCounts) {
        final TradingResult result = optimizer.optimize(new Scenario(description, schuurs));

        assertThat(result.maxProfit()).isEqualTo(expectedProfit);
        assertThat(result.flutCounts()).containsExactlyElementsOf(expectedCounts);
    }

    @Test
    void tellsPerSchuurHowManyBoxesToBuy() {
        assertThat(FlutTradingOptimizer.optimizeSchuur(pile(7, 3, 11, 9, 10)))
                .isEqualTo(new SchuurResult(10, List.of(2, 4, 5)));
        assertThat(FlutTradingOptimizer.optimizeSchuur(pile(20, 14, 3)))
                .isEqualTo(new SchuurResult(0, List.of(0)));
        assertThat(FlutTradingOptimizer.optimizeSchuur(pile()))
                .isEqualTo(new SchuurResult(0, List.of(0)));
    }

    @Test
    void handlesLargePiles() {
        final List<Schuur> schuurs = Collections.nCopies(3, pile(ones(100_000)));

        final TradingResult result = optimizer.optimize(new Scenario("many cheap boxes", schuurs));

        assertThat(result.maxProfit()).isEqualTo(3L * 100_000 * 9);
        assertThat(result.flutCounts()).containsExactly(300_000);
    }

    private static Schuur pile(final int... prices) {
        return new Schuur(Arrays.stream(prices).boxed().toList());
    }

    private static int[] tens(final int count) {
        final int[] prices = new int[count];
        Arrays.fill(prices, 10);
        return prices;
    }

    private static int[] ones(final int count) {
        final int[] prices = new int[count];
        Arrays.fill(prices, 1);
        return prices;
    }
}
