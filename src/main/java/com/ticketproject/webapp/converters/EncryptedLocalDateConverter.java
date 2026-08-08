package com.ticketproject.webapp.converters;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.services.database.CryptoService;

import java.security.GeneralSecurityException;
import java.time.LocalDate;

import javax.crypto.AEADBadTagException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * EncryptedLocalDateConverter is used to handle encryption and decryption of
 * LocalDate objects as they are stored into and retrieved from the database.
 */
@Converter
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, byte[]>
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
     * Takes a LocalDate object, encrypts it, and returns a byte[] of the ciphertext.
     */
    @Override
    public byte[] convertToDatabaseColumn(LocalDate date)
    {
        if (date == null)
            return null;
        try
        {
            return getCryptoService().encryptLocalDate(date);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to encrypt LocalDate", e);
        }
    }

    /**
     * Takes a byte[] of ciphertext, decrypts it, and constructs a
     * LocalDate object from the resulting plaintext.
     */
    @Override
    public LocalDate convertToEntityAttribute(byte[] ciphertext)
    {
        if (ciphertext == null || ciphertext.length == 0)
            return null;
        try
        {
            return getCryptoService().decryptLocalDate(ciphertext);
        }
        catch (AEADBadTagException e)
        {
            throw new RuntimeException("GCM authentication tag mismatch - wrong key or tampered data", e);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to decrypt LocalDate", e);
        }
    }
}