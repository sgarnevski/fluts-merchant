package com.fluts.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;

class FlutTradingOptimizerTest {

    private final FlutTradingOptimizer optimizer = new FlutTradingOptimizer();

    @Nested
    class SpecificationExamples {

        @Test
        void example1EarnsEightFlorinsByBuyingTheTopFourBoxes() {
            assertThat(optimize(pile(12, 3, 10, 7, 16, 5))).isEqualTo(result(8, 4));
        }

        @Test
        void example2EarnsFortyFlorinsWithSevenPossibleNumbersOfFluts() {
            assertThat(optimize(pile(7, 3, 11, 9, 10), pile(1, 2, 3, 4, 10, 16, 10, 4, 16)))
                    .isEqualTo(result(40, 6, 7, 8, 9, 10, 12, 13));
        }
    }

    @Nested
    class SinglePile {

        @Test
        void buysTheTopBoxesUpToTheMostProfitablePoint() {
            assertThat(optimize(pile(1, 2, 15))).isEqualTo(result(17, 2));
        }

        @Test
        void buysAnExpensiveBoxWhenTheCheapBoxesBelowItMoreThanPayForIt() {
            assertThat(optimize(pile(12, 1))).isEqualTo(result(7, 2));
        }

        @Test
        void buysNothingWhenTheCheapBoxesBelowDoNotPayForTheExpensiveOneOnTop() {
            assertThat(optimize(pile(20, 1))).isEqualTo(result(0, 0));
        }

        @Test
        void buysNothingWhenEveryBoxCostsMoreThanAFlutSellsFor() {
            assertThat(optimize(pile(11, 12, 20))).isEqualTo(result(0, 0));
        }

        @Test
        void listsEveryNumberOfBoxesThatReachesTheMaximum() {
            assertThat(optimize(pile(5, 15, 5))).isEqualTo(result(5, 1, 3));
        }

        @Test
        void treatsBoxesPricedExactlyTenAsOptional() {
            assertThat(optimize(pile(10, 10))).isEqualTo(result(0, 0, 1, 2));
        }

        @Test
        void buysNothingFromAnEmptyPile() {
            assertThat(optimize(pile())).isEqualTo(result(0, 0));
        }

        @Test
        void tellsTheBestBuyOfOnePileOnItsOwn() {
            assertThat(FlutTradingOptimizer.optimizeSchuur(pile(7, 3, 11, 9, 10)))
                    .isEqualTo(new SchuurResult(10, List.of(2, 4, 5)));
        }
    }

    @Nested
    class SeveralPiles {

        @Test
        void addsUpTheMaximumProfitOfEachPile() {
            assertThat(optimize(pile(1), pile(2), pile(3)).maxProfit()).isEqualTo(9 + 8 + 7);
        }

        @Test
        void combinesOneOptimalNumberOfBoxesFromEveryPile() {
            assertThat(optimize(pile(10), pile(5, 15, 5))).isEqualTo(result(5, 1, 2, 3, 4));
        }

        @Test
        void ignoresPilesThatOnlyLoseMoney() {
            assertThat(optimize(pile(20, 30), pile(4), pile())).isEqualTo(result(6, 1));
        }
    }

    @Nested
    class ReportedNumbersOfFluts {

        @Test
        void reportsOnlyTheTenSmallestOfOnePile() {
            assertThat(optimize(pile(tens(12))).flutCounts()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        }

        @Test
        void reportsOnlyTheTenSmallestAcrossPiles() {
            assertThat(optimize(pile(9, 10, 10, 10, 10, 10, 10), pile(9, 10, 10, 10, 10, 10, 10)).flutCounts())
                    .containsExactly(2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        }

        @Test
        void reportsEachNumberOnceInAscendingOrder() {
            assertThat(optimize(pile(10, 10), pile(10, 10), pile(10)).flutCounts())
                    .containsExactly(0, 1, 2, 3, 4, 5).isSorted().doesNotHaveDuplicates();
        }
    }

    @Nested
    class LargeInput {

        @Test
        void doesNotOverflowWithHugePrices() {
            assertThat(optimize(pile(Integer.MAX_VALUE, Integer.MAX_VALUE, 1))).isEqualTo(result(0, 0));
        }

        @Test
        void sumsProfitsBeyondTheIntRange() {
            final List<Schuur> piles = Collections.nCopies(3, pile(ones(100_000)));

            final TradingResult result = optimizer.optimize(new Scenario("many cheap boxes", piles));

            assertThat(result).isEqualTo(new TradingResult(3L * 100_000 * 9, List.of(300_000)));
        }
    }

    @Nested
    class InvalidInput {

        @Test
        void rejectsAMissingScenario() {
            assertThatThrownBy(() -> optimizer.optimize(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void rejectsAMissingSchuur() {
            assertThatThrownBy(() -> FlutTradingOptimizer.optimizeSchuur(null)).isInstanceOf(NullPointerException.class);
        }
    }

    /**
     * The optimizer against an independent reference: try every combination of "top k boxes" per
     * pile, keep the best profit and every total that reaches it. Random small scenarios, fixed seeds.
     */
    @Nested
    class AgreesWithBruteForce {

        @RepeatedTest(200)
        void onRandomSmallScenarios(final RepetitionInfo repetition) {
            final Random random = new Random(repetition.getCurrentRepetition());
            final List<Schuur> piles = IntStream.range(0, random.nextInt(1, 5))
                    .mapToObj(index -> pile(random.ints(random.nextInt(0, 7), 1, 16).toArray()))
                    .toList();

            assertThat(optimize(piles.toArray(Schuur[]::new))).isEqualTo(bruteForce(piles));
        }

        private static TradingResult bruteForce(final List<Schuur> piles) {
            final int[] sizes = piles.stream().mapToInt(pile -> pile.boxPrices().size() + 1).toArray();
            final int combinations = Arrays.stream(sizes).reduce(1, (a, b) -> a * b);
            final long[] profits = new long[combinations];
            final int[] counts = new int[combinations];
            for (final int combination : IntStream.range(0, combinations).toArray()) {
                final int[] digits = digits(combination, sizes);
                for (final int index : IntStream.range(0, piles.size()).toArray()) {
                    final List<Integer> prices = piles.get(index).boxPrices().subList(0, digits[index]);
                    profits[combination] += prices.stream().mapToLong(price -> 10L - price).sum();
                    counts[combination] += digits[index];
                }
            }
            final long best = Arrays.stream(profits).max().orElseThrow();
            final SortedSet<Integer> optimal = new TreeSet<>();
            IntStream.range(0, combinations).filter(c -> profits[c] == best).forEach(c -> optimal.add(counts[c]));
            return new TradingResult(best, optimal.stream().limit(FlutTradingOptimizer.MAX_REPORTED_COUNTS).toList());
        }

        /** The mixed-radix digits of {@code number}: one "k" per pile. */
        private static int[] digits(final int number, final int[] radixes) {
            final int[] digits = new int[radixes.length];
            final int[] rest = {number};
            IntStream.range(0, radixes.length).forEach(index -> {
                digits[index] = rest[0] % radixes[index];
                rest[0] /= radixes[index];
            });
            return digits;
        }
    }

    private TradingResult optimize(final Schuur... piles) {
        return optimizer.optimize(new Scenario("test", List.of(piles)));
    }

    private static TradingResult result(final long maxProfit, final Integer... flutCounts) {
        return new TradingResult(maxProfit, List.of(flutCounts));
    }

    private static Schuur pile(final int... prices) {
        return new Schuur(Arrays.stream(prices).boxed().toList());
    }

    private static int[] tens(final int count) {
        return IntStream.generate(() -> 10).limit(count).toArray();
    }

    private static int[] ones(final int count) {
        return IntStream.generate(() -> 1).limit(count).toArray();
    }
}
