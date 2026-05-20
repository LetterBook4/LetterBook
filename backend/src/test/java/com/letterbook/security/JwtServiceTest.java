package com.letterbook.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    JwtService svc = new JwtService("0123456789abcdef0123456789abcdef-extra-padding-32b", 60);

    @Test void roundTripSubject() {
        String tok = svc.generate("user-1", "u@x.com");
        assertEquals("user-1", svc.parseSubject(tok));
    }

    @Test void invalidTokenThrows() {
        assertThrows(Exception.class, () -> svc.parseSubject("not-a-jwt"));
    }

    @Test void shortSecretThrows() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 10));
    }
}
