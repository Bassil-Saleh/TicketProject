package com.ticketproject.webapp.model.converters;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.AEADBadTagException;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedPrivateKeyConverter implements AttributeConverter<PrivateKey, byte[]>
{
    private CryptoService cryptoService;

    private CryptoService getCryptoService()
    {
        if (cryptoService == null)
            cryptoService = SpringContextBridge.getBean(CryptoService.class);
        return cryptoService;
    }

    @Override
    public byte[] convertToDatabaseColumn(PrivateKey privateKey)
    {
        if (privateKey == null)
            return null;
        byte[] bytes = privateKey.getEncoded();
        try
        {
            return getCryptoService().encrypt(bytes);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to encrypt PrivateKey", e);
        }
    }

    @Override
    public PrivateKey convertToEntityAttribute(byte[] ciphertext)
    {
        if (ciphertext == null || ciphertext.length == 0)
            return null;
        try
        {
            byte[] bytes = getCryptoService().decrypt(ciphertext);
            KeyFactory keyFactory = KeyFactory.getInstance(AppConstants.Crypto.PUBLIC_PRIVATE_KEY_ALGORITHM);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        }
        catch (AEADBadTagException e)
        {
            throw new RuntimeException("GCM authentication tag mismatch - wrong key or tampered data", e);
        }
        catch (GeneralSecurityException e)
        {
            throw new RuntimeException("Failed to decrypt PrivateKey", e);
        }
    }
}