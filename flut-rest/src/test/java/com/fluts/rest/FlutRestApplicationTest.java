package com.fluts.rest;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;

class FlutRestApplicationTest {

    @Test
    void mainStartsTheApplication() {
        assertThatNoException().isThrownBy(() -> FlutRestApplication.main(new String[] {
            "--spring.main.web-application-type=none", "--spring.main.banner-mode=off"}));
    }
}
