package com.ticketproject.webapp.services.jwt;

import com.ticketproject.webapp.constants.AppConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

/**
 * JwtService is used to generate and validate JSON Web Tokens (JWTs)
 * for authenticating event host login sessions.
 * 
 * JWTs are signed using HMAC-SHA256 with a symmetric secret key
 * loaded from the application configuration.
 */
@Component
public class JwtService
{
    private final SecretKey signingKey;

    /**
     * Constructor that takes a secret key encoded in Base64.
     * @param keyBase64 secret key encoded in Base64. NEVER hardcode it in your code!
     * @throws IllegalArgumentException if the key's length is incorrect
     */
    public JwtService(@Value("${app.jwt.secret-base64}") String keyBase64)
    throws IllegalArgumentException
    {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        if (keyBytes.length < 32)
        {
            throw new IllegalArgumentException
            ("JWT signing key must be at least 256 bits (32 bytes). Got: " + (keyBytes.length * 8) + " bits");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a signed JWT containing the raw session token as its subject.
     * 
     * The JWT includes:
     * - Subject: the raw session token (used to look up the Session entity)
     * - Issued-at: the current time
     * - Expiration: the session's expiration time
     * - Issuer: the application name
     * - Digital signature: HMAC-SHA256 using the configured secret key
     * 
     * @param rawSessionToken the raw session token to include as the JWT subject
     * @param sessionExpiration the expiration time for the session
     * @return a signed JWT string
     */
    public String generateToken(String rawSessionToken, LocalDateTime sessionExpiration)
    {
        Instant now = Instant.now();
        Instant expiration = sessionExpiration.atZone(ZoneId.systemDefault()).toInstant();

        return Jwts.builder()
            .subject(rawSessionToken)
            .issuer(AppConstants.Jwt.ISSUER)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(this.signingKey)
            .compact();
    }

    /**
     * Validate a JWT and extract the raw session token from its subject claim.
     * 
     * This method verifies:
     * - The digital signature matches (token was signed by this server)
     * - The token has not expired
     * - The issuer matches the expected value
     * 
     * @param jwt the JWT string to validate
     * @return the raw session token (subject claim) if validation succeeds
     * @throws JwtException if the JWT is invalid, expired, or tampered with
     */
    public String validateToken(String jwt) throws JwtException
    {
        Claims claims = Jwts.parser()
            .verifyWith(this.signingKey)
            .requireIssuer(AppConstants.Jwt.ISSUER)
            .build()
            .parseSignedClaims(jwt)
            .getPayload();

        return claims.getSubject();
    }
}