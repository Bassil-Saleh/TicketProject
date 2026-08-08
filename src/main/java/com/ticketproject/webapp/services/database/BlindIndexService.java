package com.ticketproject.webapp.services.database;

import com.ticketproject.webapp.constants.AppConstants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * BlindIndexService is used to compute blind indexes to database table fields
 * that should be stored as ciphertext, but still require certain queries
 * (i.e. checking for uniqueness) to be performed with those fields.
 */
@Component
public class BlindIndexService
{
    // This must be different from the AES encryption key
    // you chose for this application.
    private final SecretKey hmacKey;

    /**
     * Constructor which accepts a secret key encoded in Base64.
     * @param keyBase64 secret key encoded in Base64. Do NOT hardcode it in your code!
     * @throws IllegalArgumentException if keyBase64 is null
     * or if the key's length is not long enough
     */
    public BlindIndexService(@Value("${app.blind-index.key-base64}") String keyBase64)
    throws IllegalArgumentException
    {
        if (keyBase64 == null)
            throw new IllegalArgumentException("HMAC key cannot be null");
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        if (keyBytes.length < 32)
        {
            throw new IllegalArgumentException("HMAC key must be at least 256 bits");
        }
        this.hmacKey = new SecretKeySpec(keyBytes, AppConstants.Crypto.BLIND_INDEX_KEY_ALGORITHM);
    }

    /**
     * Given a plaintext String, compute a blind index from it.
     * @param value the plaintext String
     * @return the blind index's bytes
     * @throws RuntimeException if the blind index computation fails
     */
    public byte[] computeIndex(String value)
    {
        if (value == null)
            return null;
        String normalized = normalize(value);
        try
        {
            Mac mac = Mac.getInstance(AppConstants.Crypto.BLIND_INDEX_KEY_ALGORITHM);
            mac.init(this.hmacKey);
            return mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e)
        {
            throw new RuntimeException("Blind index computation failed - check algorithm name and HMAC key", e);
        }
    }

    /**
     * Given a String, remove trailing and leading whitespace, turn all the letters
     * into lowercase letters and return the resulting String.
     * @param value a String
     * @return the normalized String
     */
    private String normalize(String value)
    {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
