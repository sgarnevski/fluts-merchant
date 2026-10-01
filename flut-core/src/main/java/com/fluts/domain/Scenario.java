package com.fluts.domain;

import java.util.List;
import java.util.Objects;

/**
 * One trading scenario: the schuurs a merchant finds on Timboektoe.
 *
 * @param name    human-readable name, never blank
 * @param schuurs the piles, at least one
 */
public record Scenario(String name, List<Schuur> schuurs) {

    public Scenario {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Scenario name must not be blank");
        }
        schuurs = List.copyOf(Objects.requireNonNull(schuurs, "schuurs"));
        if (schuurs.isEmpty()) {
            throw new IllegalArgumentException("A scenario needs at least one schuur");
        }
    }
}
