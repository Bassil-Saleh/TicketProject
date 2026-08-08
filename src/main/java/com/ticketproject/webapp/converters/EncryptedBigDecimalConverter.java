package com.ticketproject.webapp.converters;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.services.database.CryptoService;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import javax.crypto.AEADBadTagException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * EncryptedBigDecimalConverter is used to handle encryption and decryption of
 * BigDecimal objects as they are stored into and retrieved from the database.
 */
@Converter
public class EncryptedBigDecimalConverter implements AttributeConverter<BigDecimal, byte[]>
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
     * Takes a BigDecimal object, encrypts it, and returns the
     * ciphertext as a byte[].
     */
    @Override
    public byte[] convertToDatabaseColumn(BigDecimal decimal)
    {
        if (decimal == null)
            return null;
        try
        {
            return getCryptoService().encryptBigDecimal(decimal);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to encrypt BigDecimal", e);
        }
    }

    /**
     * Takes a byte[] of ciphertext, decrypts it, and constructs
     * a BigDecimal object from the resulting plaintext.
     */
    @Override
    public BigDecimal convertToEntityAttribute(byte[] ciphertext)
    {
        if (ciphertext == null || ciphertext.length == 0)
            return null;
        try
        {
            return getCryptoService().decryptBigDecimal(ciphertext);
        }
        catch (AEADBadTagException e)
        {
            throw new RuntimeException("GCM authentication tag mismatch - wrong key or tampered data", e);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to decrypt BigDecimal", e);
        }
    }
}