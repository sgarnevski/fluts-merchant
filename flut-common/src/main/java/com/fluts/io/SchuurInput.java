package com.fluts.io;

import java.util.List;

/**
 * One schuur in the normalized JSON input.
 *
 * @param boxPrices prices of the boxes, top to bottom; may be empty
 */
public record SchuurInput(List<Integer> boxPrices) {
}
