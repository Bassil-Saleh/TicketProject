package com.ticketproject.webapp.model.converters;

import java.security.GeneralSecurityException;
import java.time.LocalDate;

import javax.crypto.AEADBadTagException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedLocalDateConverter implements AttributeConverter<LocalDate, byte[]>
{
    private CryptoService cryptoService;

    private CryptoService getCryptoService()
    {
        if (cryptoService == null)
            cryptoService = SpringContextBridge.getBean(CryptoService.class);
        return cryptoService;
    }

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