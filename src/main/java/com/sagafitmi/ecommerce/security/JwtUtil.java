package com.sagafitmi.ecommerce.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Component
public class JwtUtil {

    private final String jwtSecret;
    private final long jwtExpirationMinutes;

    public JwtUtil(@Value("${app.security.jwtSecret:changeit}") String jwtSecret,
            @Value("${app.security.jwtExpirationMinutes:5}") long jwtExpirationMinutes) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMinutes = jwtExpirationMinutes;
    }

    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Algorithm alg = Algorithm.HMAC256(jwtSecret.getBytes());

        return JWT.create()
                .withSubject(username)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(jwtExpirationMinutes, ChronoUnit.MINUTES)))
                .withClaim("roles", roles)
                .sign(alg);
    }

    // Método para futuras validaciones
    public Algorithm getAlgorithm() {
        return Algorithm.HMAC256(jwtSecret.getBytes());
    }
}
