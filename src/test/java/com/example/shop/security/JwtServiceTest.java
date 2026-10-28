package com.example.shop.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "0123456789-0123456789-0123456789-abcd";

    private final JwtService service = new JwtService(SECRET, 60, Clock.systemUTC());

    @Test
    void token_roundTrip_returnsTheUsername() {
        assertThat(service.extractUsername(service.generateToken("alice"))).isEqualTo("alice");
    }

    @Test
    void tamperedToken_isRejected() {
        String token = service.generateToken("alice");

        assertThatThrownBy(() -> service.extractUsername(token + "x")).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecret_isRejected() {
        JwtService other = new JwtService("another-secret-another-secret-another-secret", 60, Clock.systemUTC());

        assertThatThrownBy(() -> service.extractUsername(other.generateToken("alice")))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredToken_isRejected() {
        Clock twoHoursAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), java.time.ZoneOffset.UTC);
        String token = new JwtService(SECRET, 60, twoHoursAgo).generateToken("alice");

        assertThatThrownBy(() -> service.extractUsername(token)).isInstanceOf(JwtException.class);
    }
}
