package com.ticketproject.webapp.model.converters;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;

import javax.crypto.AEADBadTagException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedBigDecimalConverter implements AttributeConverter<BigDecimal, byte[]>
{
    private CryptoService cryptoService;

    private CryptoService getCryptoService()
    {
        if (cryptoService == null)
            cryptoService = SpringContextBridge.getBean(CryptoService.class);
        return cryptoService;
    }

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