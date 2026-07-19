package com.ticketproject.webapp.model.converters;

import com.ticketproject.webapp.constants.AppConstants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@Component
public class CryptoService
{

    private final SecretKey secretKey;

    // Load your key from your application's configuration or from a keystore.
    // NEVER hardcode your key into the source code!
    public CryptoService(@Value("${app.encryption.key-base64}") String keyBase64)
    throws IllegalArgumentException
    {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32)
        {
            throw new IllegalArgumentException
            ("AES key must be 128, 192, or 256 bits. Got: " + (keyBytes.length * 8));
        }
        this.secretKey = new SecretKeySpec
        (keyBytes, AppConstants.Crypto.SECRET_KEY_ALGORITHM);
    }

    /**
     * Encrypt plaintext bytes into IV + ciphertext + authentication tag.
     * @param plaintext bytes to be encrypted
     * @return bytes of IV + ciphertext + authentication tag
     * @throws GeneralSecurityException if the encryption fails at any point
     */
    public byte[] encrypt(byte[] plaintext) throws GeneralSecurityException
    {
        try
        {
            byte[] iv = generateIv();
            Cipher cipher =
                Cipher.getInstance(AppConstants.Crypto.ENCRYPTION_ALGORITHM);
            cipher.init
            (
                Cipher.ENCRYPT_MODE,
                this.secretKey,
                new GCMParameterSpec(AppConstants.Crypto.GCM_TAG_LENGTH_BITS, iv)
            );

            // If GCM is specified in the encryption algorithm, then this will
            // append an authentication tag to the ciphertext.
            byte[] ciphertextWithTag = cipher.doFinal(plaintext);

            ByteBuffer byteBuffer =
                ByteBuffer.allocate(iv.length + ciphertextWithTag.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertextWithTag);
            return byteBuffer.array();
        }
        catch (GeneralSecurityException e)
        {
            throw new GeneralSecurityException("Encryption failed", e);
        }
    }

    /**
     * Encrypt a String into IV + ciphertext.
     * @param plaintext the String to be encrypted
     * @return a byte[] of the IV + ciphertext
     * @throws GeneralSecurityException if the encryption process fails
     */
    public byte[] encryptString(String plaintext) throws GeneralSecurityException
    {
        if (plaintext == null)
            return null;
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decrypts bytes of IV + ciphertext into plaintext bytes
     * @param combined the byte array to be decrypted
     * @return a byte array of plaintext
     * @throws AEADBadTagException if a GCM authentication tag
     * mismatch is detected (can be caused if the data was tampered
     * with, or if an incorrect key was used)
     * @throws GeneralSecurityException if decryption fails for a different reason
     */
    public byte[] decrypt(byte[] combined)
    throws AEADBadTagException, GeneralSecurityException
    {
        try
        {
            ByteBuffer byteBuffer = ByteBuffer.wrap(combined);

            byte[] iv = new byte[AppConstants.Crypto.GCM_IV_LENGTH_BYTES];
            byteBuffer.get(iv);

            byte[] ciphertextWithTag = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertextWithTag);

            Cipher cipher = Cipher.getInstance(AppConstants.Crypto.ENCRYPTION_ALGORITHM);
            cipher.init
            (
                Cipher.DECRYPT_MODE,
                this.secretKey,
                new GCMParameterSpec(AppConstants.Crypto.GCM_TAG_LENGTH_BITS, iv)
            );

            return cipher.doFinal(ciphertextWithTag);
        }
        catch (AEADBadTagException e)
        {
            throw new AEADBadTagException("Data tampered or wrong key - GCM authentication tag mismatch");
        }
        catch (GeneralSecurityException e)
        {
            throw new GeneralSecurityException("Decryption failed", e);
        }
    }

    /**
     * Decrypt bytes of IV + ciphertext and return the plaintext as a String.
     * If null is provided, return null.
     * @param combined the byte array to be decrypted
     * @return a String object of the plaintext contents
     * @throws AEADBadTagException if a GCM authentication tag
     * mismatch is detected (can be caused if the data was tampered
     * with, or if an incorrect key was used)
     * @throws GeneralSecurityException if decryption fails for a different reason
     */
    public String decryptString(byte[] combined)
    throws AEADBadTagException, GeneralSecurityException
    {
        if (combined == null)
            return null;
        return new String(decrypt(combined), StandardCharsets.UTF_8);
    }

    /**
     * Generate a byte array containing bytes of an initialization vector.
     * @return a byte array containing bytes of an initialization vector
     */
    private byte[] generateIv()
    {
        byte[] iv = new byte[AppConstants.Crypto.GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}