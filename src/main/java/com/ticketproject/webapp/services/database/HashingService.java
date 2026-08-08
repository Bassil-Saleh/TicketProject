package com.ticketproject.webapp.services.database;

import com.ticketproject.webapp.constants.AppConstants;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * HashingService is used to compute hashes for database table data, as well as
 * generate cryptographically random tokens (and their corresponding hashes).
 */
@Component
public class HashingService
{
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor that initializes the password encoder.
     */
    public HashingService()
    {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Hash a plaintext password and return a bcrypt String.
     * @param plaintextPassword a plaintext password
     * @return a bcrypt String
     * @throws IllegalArgumentException if plaintextPassword is null, empty, or blank
     */
    public String hashPassword(String plaintextPassword) throws IllegalArgumentException
    {
        if (plaintextPassword == null || plaintextPassword.isBlank())
        {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return passwordEncoder.encode(plaintextPassword);
    }

    /**
     * Verify a plaintext password against a stored bcrypt hash.
     * @param plaintextPassword a plaintext password
     * @param storedHash a stored bcrypt hash
     * @return true if the plaintext password, after hashing, matches the stored hash
     */
    public boolean verifyPassword(String plaintextPassword, String storedHash)
    {
        if (plaintextPassword == null || storedHash == null)
            return false;
        return passwordEncoder.matches(plaintextPassword, storedHash);
    }

    /**
     * Given a byte array of arbitrary input, compute its hash.
     * @param tokenBytes a byte array of arbitrary input
     * @return a byte array storing the computed hash
     * @throws RuntimeException if the algorithm
     * requested by this method is not available
     */
    public byte[] hashToken(byte[] tokenBytes)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance(AppConstants.Crypto.HASH_ALGORITHM);
            return digest.digest(tokenBytes);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(AppConstants.Crypto.HASH_ALGORITHM + " not available", e);
        }
    }

    /**
     * An overload of the hashToken() method for convenience.
     * @param token a String to be hashed.
     * @return a byte array storing the computed hash
     * @throws RuntimeException if the algorithm
     * requested by hashToken() is not available
     */
    public byte[] hashToken(String token)
    {
        return this.hashToken(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a cryptographically random token string.
     * Used for email verification links or password reset URLs.
     * @return a cryptographically random token string
     */
    public String generateRandomToken()
    {
        byte[] bytes = new byte[AppConstants.Crypto.RANDOM_TOKEN_LENGTH_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * GeneratedToken is a record for storing a token and its corresponding hash.
     * @param rawToken a plaintext token (i.e. cryptographically random bytes)
     * @param tokenHash a byte array of the plaintext token's hash
     */
    public record GeneratedToken(String rawToken, byte[] tokenHash) {}

    /**
     * Generate a cryptographically random token to be sent to the user
     * (i.e. for a new account verification link) and its SHA-256 hash
     * (to be stored in the database).
     * @return a GeneratedToken record containing both the raw token and the token's hash.
     */
    public GeneratedToken generateVerificationToken()
    {
        String rawToken = this.generateRandomToken();
        byte[] tokenHash = this.hashToken(rawToken);
        return new GeneratedToken(rawToken, tokenHash);
    }

    /**
     * Verify a token provided by the user against a stored hash.
     * @param rawToken the raw token provided by the user
     * @param storedHash a hash from the database
     * @return true if the raw token, after hashing, matches the stored hash
     */
    public boolean verifyToken(String rawToken, byte[] storedHash)
    {
        if (rawToken == null || storedHash == null)
            return false;
        byte[] computedHash = this.hashToken(rawToken);
        return MessageDigest.isEqual(computedHash, storedHash);
    }
}
