package com.ticketproject.webapp.converters;

import java.security.GeneralSecurityException;

import javax.crypto.AEADBadTagException;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.services.database.CryptoService;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * EncryptedStringConverter is used to handle encryption and decryption of
 * String objects as they are stored into and retrieved from the database.
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, byte[]>
{
    private CryptoService cryptoService;

    /**
     * Retrieves the encryption service from Spring's application context.
     * @return a reference to the encryption service
     */
    private CryptoService getCryptoService()
    {
        if (cryptoService == null)
            cryptoService = SpringContextBridge.getBean(CryptoService.class);
        return cryptoService;
    }

    /**
     * Takes a String object, encrypts it, and returns a byte[] of the resulting plaintext.
     */
    @Override
    public byte[] convertToDatabaseColumn(String plaintext)
    {
        if (plaintext == null || plaintext.isEmpty())
            return null;
        try
        {
            return getCryptoService().encryptString(plaintext);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to encrypt String", e);
        }
    }

    /**
     * Takes a byte[] of ciphertext, decrypts it, and constructs a
     * String object from the resulting plaintext.
     */
    @Override
    public String convertToEntityAttribute(byte[] ciphertext)
    {
        if (ciphertext == null || ciphertext.length == 0)
            return null;
        try
        {
            return getCryptoService().decryptString(ciphertext);
        }
        catch (AEADBadTagException e)
        {
            throw new RuntimeException("GCM authentication tag mismatch - wrong key or tampered data", e);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to decrypt String", e);
        }
    }
}