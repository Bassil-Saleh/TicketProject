package com.ticketproject.webapp.model.converters;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

import javax.crypto.AEADBadTagException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedLocalDateTimeConverter implements AttributeConverter<LocalDateTime, byte[]>
{
    private CryptoService cryptoService;

    private CryptoService getCryptoService()
    {
        if (cryptoService == null)
            cryptoService = SpringContextBridge.getBean(CryptoService.class);
        return cryptoService;
    }

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