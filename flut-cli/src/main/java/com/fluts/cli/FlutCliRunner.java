package com.fluts.cli;

import com.fluts.domain.TradingResult;
import com.fluts.io.ScenarioParseException;
import com.fluts.io.TextResultFormatter;
import com.fluts.io.TextScenarioParser;
import com.fluts.trading.Evaluation;
import com.fluts.trading.TradingService;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

/** Reads the input, solves every scenario and prints the results; remembers the exit code. */
@Component
public class FlutCliRunner implements ApplicationRunner, ExitCodeGenerator {

    static final int OK = 0;
    static final int INVALID_INPUT = 1;
    static final int INPUT_NOT_READABLE = 2;

    private static final String USAGE = "Usage: java -jar flut-cli.jar [input-file]   (reads stdin without a file)";

    private final ConsoleStreams console;
    private final TextScenarioParser parser;
    private final TradingService tradingService;
    private final TextResultFormatter formatter;
    private final AtomicInteger exitCode = new AtomicInteger(OK);

    public FlutCliRunner(final ConsoleStreams console, final TextScenarioParser parser,
            final TradingService tradingService, final TextResultFormatter formatter) {
        this.console = console;
        this.parser = parser;
        this.tradingService = tradingService;
        this.formatter = formatter;
    }

    @Override
    public void run(final ApplicationArguments args) {
        final List<String> files = args.getNonOptionArgs();
        if (files.size() > 1) {
            fail(INPUT_NOT_READABLE, USAGE);
            return;
        }
        try {
            final List<TradingResult> results = files.isEmpty() ? solve(console.in()) : solve(Path.of(files.getFirst()));
            console.out().print(formatter.format(results));
        } catch (final ScenarioParseException e) {
            fail(INVALID_INPUT, "Invalid input: " + e.getMessage());
        } catch (final NoSuchFileException e) {
            fail(INPUT_NOT_READABLE, "Cannot read input: file not found: " + e.getFile());
        } catch (final IOException e) {
            fail(INPUT_NOT_READABLE, "Cannot read input: " + e.getMessage());
        } catch (final UncheckedIOException e) {
            fail(INPUT_NOT_READABLE, "Cannot read input: " + e.getCause().getMessage());
        }
    }

    @Override
    public int getExitCode() {
        return exitCode.get();
    }

    private List<TradingResult> solve(final Path file) throws IOException {
        try (InputStream input = Files.newInputStream(file)) {
            return solve(input);
        }
    }

    private List<TradingResult> solve(final InputStream input) {
        return tradingService.evaluate(parser, input).stream().map(Evaluation::result).toList();
    }

    private void fail(final int code, final String message) {
        console.err().println(message);
        exitCode.set(code);
    }
}
