package com.fluts.io;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InputTypeTest {

    /** The names are part of the REST API, so renaming one is a breaking change. */
    @Test
    void knowsExactlyJsonBodiesAndUploadedFiles() {
        assertThat(InputType.values()).extracting(InputType::name).containsExactly("JSON", "FILE");
    }
}
