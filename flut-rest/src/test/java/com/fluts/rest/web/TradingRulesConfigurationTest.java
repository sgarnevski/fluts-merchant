package com.fluts.rest.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** The rules of the trade are configuration: here a flut sells for 15 florins and only two counts are reported. */
@SpringBootTest(properties = {"flut.trading.selling-price=15", "flut.trading.max-reported-counts=2"})
@AutoConfigureMockMvc
class TradingRulesConfigurationTest {

    private final MockMvcTester mvc;

    TradingRulesConfigurationTest(@Autowired final MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void evaluatesWithTheConfiguredSellingPriceAndNumberOfCounts() {
        assertThat(mvc.post().uri("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scenarios\": [{\"schuurs\": [{\"boxPrices\": [12, 15, 15, 15]}]}]}"))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("{\"results\": [{\"maxProfit\": 3, \"flutCounts\": [1, 2]}]}");
    }
}
