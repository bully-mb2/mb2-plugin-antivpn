package com.templars_server.whitelist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class WhitelistTest {

    private static Whitelist whitelist;
    private static final List<String> TEST_CONTENT = List.of(new String[]{
            "192.168.1.1",
            "10.10.*.*",
            "555.*",
            "999.999.999.*"
    });

    @BeforeEach
    void setup() throws IOException {
        whitelist = new Whitelist();
        whitelist.setContent(TEST_CONTENT);
    }

    @Test
    void testContains_UnknownIP_ReturnsFalse() {
        assertThat(whitelist.contains("111.111.111.111")).isFalse();
    }

    @Test
    void testContains_InvalidIP_ReturnsFalse() {
        assertThat(whitelist.contains("NOT_AN_IP.a.aaa.a.a.a.a.a.")).isFalse();
    }

    @Test
    void testContains_InvalidIPJustOutOfRange_ReturnsFalse() {
        assertThat(whitelist.contains("556.10.10.10")).isFalse();
    }

    @Test
    void testContains_InvalidIPNonNumeric_ReturnsFalse() {
        assertThat(whitelist.contains("*.*.*.*")).isFalse();
    }

    @Test
    void testContains_ValidIP_ReturnsTrue() {
        assertThat(whitelist.contains("192.168.1.1")).isTrue();
    }

    @Test
    void testContains_ValidMultiWildcardIP_ReturnsTrue() {
        assertThat(whitelist.contains("10.10.10.10")).isTrue();
    }

    @Test
    void testContains_ValidLargeWildcardIP_ReturnsTrue() {
        assertThat(whitelist.contains("555.1.1.1")).isTrue();
    }

    @Test
    void testContains_ValidWildcardIP_ReturnsTrue() {
        assertThat(whitelist.contains("999.999.999.1")).isTrue();
    }
}
