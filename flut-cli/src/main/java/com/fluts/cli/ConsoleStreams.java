package com.fluts.cli;

import java.io.InputStream;
import java.io.PrintStream;

/** The process streams, as a bean, so tests can run the app against in-memory streams. */
public record ConsoleStreams(InputStream in, PrintStream out, PrintStream err) {
}
