package com.ticketproject.webapp.model.converters;

import java.security.GeneralSecurityException;

import javax.crypto.AEADBadTagException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, byte[]>
{
    private CryptoService cryptoService;

    private CryptoService getCryptoService()
    {
        if (cryptoService == null)
            cryptoService = SpringContextBridge.getBean(CryptoService.class);
        return cryptoService;
    }

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
            throw new RuntimeException("Failed to encrypt field", e);
        }
    }

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
            throw new RuntimeException("Failed to decrypt field", e);
        }
    }
}