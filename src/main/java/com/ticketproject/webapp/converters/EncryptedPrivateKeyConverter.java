package com.ticketproject.webapp.converters;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.AEADBadTagException;

import com.ticketproject.webapp.bridges.SpringContextBridge;
import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.services.CryptoService;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * EncryptedPrivateKeyConverter is used to handle encryption and decryption of
 * PrivateKey objects as they are stored into and retrieved from the database.
 */
@Converter
public class EncryptedPrivateKeyConverter implements AttributeConverter<PrivateKey, byte[]>
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
     * Takes a PrivateKey object, encrypts it, and returns a byte[] of the ciphertext.
     */
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

    /**
     * Takes a byte[] of ciphertext, decrypts it, and constructs a
     * PrivateKey object from the resulting plaintext.
     */
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