package com.fluts.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Console entry point: {@code java -jar flut-cli.jar [input-file]}. Reads the original text input
 * (from the file, or stdin without one), prints the original text output and exits with
 * {@code 0} (ok), {@code 1} (invalid input) or {@code 2} (input not readable / wrong usage).
 */
@SpringBootApplication
public class FlutCliApplication {

    public static void main(final String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(FlutCliApplication.class, args)));
    }
}
