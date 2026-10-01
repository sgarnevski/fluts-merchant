package com.fluts.domain;

import java.util.List;
import java.util.Objects;

/**
 * One pile of flut boxes.
 *
 * @param boxPrices prices of the boxes, top to bottom; a merchant can only buy from the top down
 */
public record Schuur(List<Integer> boxPrices) {

    public Schuur {
        boxPrices = List.copyOf(Objects.requireNonNull(boxPrices, "boxPrices"));
        boxPrices.stream()
                .filter(price -> price <= 0)
                .findFirst()
                .ifPresent(price -> {
                    throw new IllegalArgumentException("Box prices must be positive, got " + price);
                });
    }
}
