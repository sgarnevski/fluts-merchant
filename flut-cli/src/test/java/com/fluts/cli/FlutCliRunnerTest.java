package com.fluts.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fluts.domain.FlutTradingOptimizer;
import com.fluts.domain.TradingRules;
import com.fluts.io.InputLimits;
import com.fluts.io.TextResultFormatter;
import com.fluts.io.TextScenarioParser;
import com.fluts.trading.TradingService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class FlutCliRunnerTest {

    private static final Path SAMPLE_INPUT = Path.of("../demo/input.txt");
    private static final Path SAMPLE_OUTPUT = Path.of("../demo/output.txt");

    @Test
    void readsStdinAndPrintsTheSpecificationOutput() throws IOException {
        final Run run = run(Files.readString(SAMPLE_INPUT));

        assertThat(run.out()).isEqualTo(Files.readString(SAMPLE_OUTPUT));
        assertThat(run.err()).isEmpty();
        assertThat(run.exitCode()).isZero();
    }

    @Test
    void readsTheFileGivenAsArgument() throws IOException {
        final Run run = run("", SAMPLE_INPUT.toString());

        assertThat(run.out()).isEqualTo(Files.readString(SAMPLE_OUTPUT));
        assertThat(run.exitCode()).isZero();
    }

    @Test
    void printsNothingForEmptyInput() {
        final Run run = run("0\n");

        assertThat(run.out()).isEmpty();
        assertThat(run.exitCode()).isZero();
    }

    @Test
    void reportsInvalidInputOnStderrWithExitCodeOne() {
        final Run run = run("1\n3 1 2\n");

        assertThat(run.out()).isEmpty();
        assertThat(run.err()).isEqualTo("Invalid input: Line 2: Schuur declares 3 boxes but lists 2 prices\n");
        assertThat(run.exitCode()).isEqualTo(1);
    }

    @Test
    void reportsMissingFileWithExitCodeTwo() {
        final Run run = run("", "does-not-exist.txt");

        assertThat(run.out()).isEmpty();
        assertThat(run.err()).startsWith("Cannot read input: ").contains("does-not-exist.txt");
        assertThat(run.exitCode()).isEqualTo(2);
    }

    @Test
    void reportsFileWithoutReadPermissionWithExitCodeTwo() throws IOException {
        final Path file = Files.createTempFile("flut-no-permission", ".txt");
        assumeTrue(file.toFile().setReadable(false) && !Files.isReadable(file), "needs a non-root user");

        final Run run = run("", file.toString());

        assertThat(run.err()).isEqualTo("Cannot read input: " + file + "\n");
        assertThat(run.exitCode()).isEqualTo(2);
    }

    @Test
    void reportsUnreadableInputWithExitCodeTwo() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        final FlutCliRunner runner = runner(new ConsoleStreams(new FailingInputStream(), print(out), print(err)));

        runner.run(new DefaultApplicationArguments());

        assertThat(err.toString(StandardCharsets.UTF_8)).isEqualTo("Cannot read input: disk on fire\n");
        assertThat(runner.getExitCode()).isEqualTo(2);
    }

    @Test
    void rejectsMoreThanOneFileWithUsageAndExitCodeTwo() {
        final Run run = run("", "a.txt", "b.txt");

        assertThat(run.err()).isEqualTo("Usage: java -jar flut-cli.jar [input-file]   (reads stdin without a file)\n");
        assertThat(run.exitCode()).isEqualTo(2);
    }

    private static Run run(final String stdin, final String... args) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        final FlutCliRunner runner = runner(new ConsoleStreams(
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), print(out), print(err)));

        runner.run(new DefaultApplicationArguments(args));

        return new Run(out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8),
                runner.getExitCode());
    }

    private static FlutCliRunner runner(final ConsoleStreams console) {
        return new FlutCliRunner(console, new TextScenarioParser(InputLimits.DEFAULT), new TradingService(new FlutTradingOptimizer(TradingRules.SPECIFICATION)),
                new TextResultFormatter());
    }

    private static PrintStream print(final ByteArrayOutputStream target) {
        return new PrintStream(target, true, StandardCharsets.UTF_8);
    }

    private record Run(String out, String err, int exitCode) {
    }

    private static final class FailingInputStream extends java.io.InputStream {
        @Override
        public int read() throws IOException {
            throw new IOException("disk on fire");
        }
    }
}
