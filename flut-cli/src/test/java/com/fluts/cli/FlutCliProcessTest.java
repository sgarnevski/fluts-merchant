package com.fluts.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * Runs the real {@code main()} in a separate JVM, exactly as a user does: stdin in, stdout and
 * the exit code out. Proves that stdout holds only the specification output (no banner, no logs).
 */
class FlutCliProcessTest {

    private static final Path SAMPLE_INPUT = Path.of("../demo/input.txt");
    private static final Path SAMPLE_OUTPUT = Path.of("../demo/output.txt");

    @Test
    void stdoutIsExactlyTheSpecificationOutput() throws Exception {
        final ProcessResult result = runMain(SAMPLE_INPUT);

        assertThat(result.stdout()).isEqualTo(Files.readString(SAMPLE_OUTPUT));
        assertThat(result.exitCode()).isZero();
    }

    @Test
    void sellingPriceCanBeConfiguredOnTheCommandLine() throws Exception {
        final Path input = Files.createTempFile("flut-dear", ".txt");
        Files.writeString(input, "1\n1 12\n0\n");

        final ProcessResult result = runMain(input, "--flut.trading.selling-price=15");

        assertThat(result.stdout()).isEqualTo("schuurs 1\nMaximum profit is 3.\nNumber of fluts to buy: 1\n");
        assertThat(result.exitCode()).isZero();
    }

    @Test
    void invalidInputExitsWithOne() throws Exception {
        final Path input = Files.createTempFile("flut-invalid", ".txt");
        Files.writeString(input, "1\n2 5\n");

        final ProcessResult result = runMain(input);

        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).contains("Invalid input: Line 2: Schuur declares 2 boxes but lists 1 prices");
        assertThat(result.exitCode()).isEqualTo(1);
    }

    @Test
    void missingFileExitsWithTwo() throws Exception {
        final ProcessResult result = runMain(Files.createTempFile("flut-empty", ".txt"), "does-not-exist.txt");

        assertThat(result.stderr()).contains("Cannot read input");
        assertThat(result.exitCode()).isEqualTo(2);
    }

    private static ProcessResult runMain(final Path stdin, final String... args)
            throws IOException, InterruptedException {
        final List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.addAll(inheritedJvmArgs());
        command.addAll(List.of("-cp", System.getProperty("java.class.path"), FlutCliApplication.class.getName()));
        command.addAll(Arrays.asList(args));

        final Path stdout = Files.createTempFile("flut-stdout", ".txt");
        final Path stderr = Files.createTempFile("flut-stderr", ".txt");
        final ProcessBuilder builder = new ProcessBuilder(command)
                .redirectOutput(stdout.toFile())
                .redirectError(stderr.toFile())
                .redirectInput(stdin.toFile());
        final Process process = builder.start();
        assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("process finished").isTrue();
        return new ProcessResult(Files.readString(stdout, StandardCharsets.UTF_8),
                Files.readString(stderr, StandardCharsets.UTF_8), process.exitValue());
    }

    /** Java agents of the test JVM (the JaCoCo agent), so the child JVM's coverage counts too. */
    private static List<String> inheritedJvmArgs() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(argument -> argument.startsWith("-javaagent:"))
                .toList();
    }

    private record ProcessResult(String stdout, String stderr, int exitCode) {
    }
}
