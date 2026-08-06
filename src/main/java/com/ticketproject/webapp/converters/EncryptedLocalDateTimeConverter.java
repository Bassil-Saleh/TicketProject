package com.ticketproject.webapp.converters;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.services.CryptoService;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

import javax.crypto.AEADBadTagException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * EncryptedLocalDateTimeConverter is used to handle encryption and decryption of
 * LocalDateTime objects as they are stored into and retrieved from the database.
 */
@Converter
public class EncryptedLocalDateTimeConverter implements AttributeConverter<LocalDateTime, byte[]>
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
     * Takes a LocalDateTime object, encrypts it, and returns a byte[] of the ciphertext.
     */
    @Override
    public byte[] convertToDatabaseColumn(LocalDateTime dateTime)
    {
        if (dateTime == null)
            return null;
        try
        {
            return getCryptoService().encryptLocalDateTime(dateTime);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to encrypt LocalDateTime", e);
        }
    }

    /**
     * Takes a byte[] of ciphertext, decrypts it, and constructs a
     * LocalDateTime object from the resulting plaintext.
     */
    @Override
    public LocalDateTime convertToEntityAttribute(byte[] ciphertext)
    {
        if (ciphertext == null || ciphertext.length == 0)
            return null;
        try
        {
            return getCryptoService().decryptLocalDateTime(ciphertext);
        }
        catch (AEADBadTagException e)
        {
            throw new RuntimeException("GCM authentication tag mismatch - wrong key or tampered data", e);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to decrypt LocalDateTime", e);
        }
    }
}