package com.aruba.zeta.userauth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Service for generating and validating internal JWT tokens using HMAC-SHA256.
 */
@Slf4j
@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final long tokenValiditySeconds;

    public JwtTokenService(
            @Value("${zeta.auth.internal.hmac-secret}") String secret,
            @Value("${zeta.auth.internal.token-validity-seconds:3600}") long tokenValiditySeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    public String generateToken(String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(tokenValiditySeconds)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates the JWT signature and expiration.
     *
     * @return {@code true} if the token is well-formed, correctly signed, and not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the userId (subject) from a token. Call {@link #validateToken} first.
     *
     * @return the userId stored in the subject claim.
     */
    public String extractUserId(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
