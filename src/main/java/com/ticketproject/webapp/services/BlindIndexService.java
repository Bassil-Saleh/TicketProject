package com.ticketproject.webapp.services;

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

@Component
public class BlindIndexService
{
    // This must be different from the AES encryption key
    // you chose for this application.
    private final SecretKey hmacKey;

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

    private String normalize(String value)
    {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
