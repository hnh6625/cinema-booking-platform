package com.hnh6625.cinema.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenHasherTest {
    @Test
    void sha256Hex_returnsKnownHashForKnownInput() {
        String input = "abc";

        String hash = TokenHasher.sha256Hex(input);

        assertThat(hash).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void sha256Hex_alwaysReturns64Characters() {
        String input = "abc";

        String hash = TokenHasher.sha256Hex(input);

        assertThat(hash).hasSize(64);
    }
}
