package com.ticketproject.webapp.services.database;

import com.ticketproject.webapp.constants.AppConstants;
import com.ticketproject.webapp.model.entities.EventSigningKey;
import com.ticketproject.webapp.model.entities.Event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * CryptoService is used to encrypt and decrypt
 * database table fields with sensitive information.
 * 
 * CryptoService also includes static methods to generate
 * EventSigningKey entities used to create digital signatures
 * for Ticket entities.
 */
@Component
public class CryptoService
{
    // Your can load your own key into this application from
    // your installation's configuration or from a keystore.
    // NEVER hardcode your key into the source code!
    private final SecretKey secretKey;

    /**
     * Constructor that takes a secret key encoded in Base64.
     * @param keyBase64 secret key encoded in Base64. NEVER hardcode it in your code!
     * @throws IllegalArgumentException if the key's length is incorrect
     */
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
     * Generate a byte array containing bytes of an initialization vector.
     * @return a byte array containing bytes of an initialization vector
     */
    private byte[] generateIv()
    {
        byte[] iv = new byte[AppConstants.Crypto.GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
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
     * Encrypt a LocalDate object.
     * @param date a LocalDate object
     * @return the resulting ciphertext bytes
     * @throws GeneralSecurityException if the encryption fails
     */
    public byte[] encryptLocalDate(LocalDate date) throws GeneralSecurityException
    {
        if (date == null)
            return null;
        return encryptString(date.format(AppConstants.Crypto.DATE_FORMAT));
    }

    /**
     * Decrypt a LocalDate object.
     * @param ciphertext the ciphertext bytes storing the LocalDate object data
     * @return the resulting LocalDate object
     * @throws AEADBadTagException  if a GCM authentication tag
     * mismatch is detected (can be caused if the data was tampered
     * with, or if an incorrect key was used)
     * @throws GeneralSecurityException if the decryption fails
     */
    public LocalDate decryptLocalDate(byte[] ciphertext)
    throws AEADBadTagException, GeneralSecurityException
    {
        if (ciphertext == null)
            return null;
        return LocalDate.parse(decryptString(ciphertext), AppConstants.Crypto.DATE_FORMAT);
    }

    /**
     * Encrypt a LocalDateTime object.
     * @param date a LocalDateTime object
     * @return the resulting ciphertext bytes
     * @throws GeneralSecurityException if the encryption fails
     */
    public byte[] encryptLocalDateTime(LocalDateTime dateTime) throws GeneralSecurityException
    {
        if (dateTime == null)
            return null;
        return encryptString(dateTime.format(AppConstants.Crypto.DATE_TIME_FORMAT));
    }

    /**
     * Decrypt a LocalDateTime object.
     * @param ciphertext the ciphertext bytes storing the LocalDateTime object data
     * @return the resulting LocalDateTime object
     * @throws AEADBadTagException  if a GCM authentication tag
     * mismatch is detected (can be caused if the data was tampered
     * with, or if an incorrect key was used)
     * @throws GeneralSecurityException if the decryption fails
     */
    public LocalDateTime decryptLocalDateTime(byte[] ciphertext)
    throws AEADBadTagException, GeneralSecurityException
    {
        if (ciphertext == null)
            return null;
        return LocalDateTime.parse(decryptString(ciphertext), AppConstants.Crypto.DATE_TIME_FORMAT);
    }

    /**
     * Encrypt a BigDecimal object.
     * @param date a BigDecimal object
     * @return the resulting ciphertext bytes
     * @throws GeneralSecurityException if the encryption fails
     */
    public byte[] encryptBigDecimal(BigDecimal value) throws GeneralSecurityException
    {
        if (value == null)
            return null;
        // toPlainString avoids scientific notation (i.e. "0.01", not "1E-2")
        return encryptString(value.toPlainString());
    }

    /**
     * Decrypt a BigDecimal object.
     * @param ciphertext the ciphertext bytes storing the BigDecimal object data
     * @return the resulting BigDecimal object
     * @throws AEADBadTagException  if a GCM authentication tag
     * mismatch is detected (can be caused if the data was tampered
     * with, or if an incorrect key was used)
     * @throws GeneralSecurityException if the decryption fails
     */
    public BigDecimal decryptBigDecimal(byte[] ciphertext)
    throws AEADBadTagException, GeneralSecurityException
    {
        if (ciphertext == null)
            return null;
        return new BigDecimal(decryptString(ciphertext));
    }

    /**
     * Helper method that generates a fresh key pair for constructing an EventSigningKey.
     * @return a new KeyPair
     * @throws RuntimeException if key pair generation fails
     */
    public static KeyPair generateKeyPair()
    {
        try
        {
            KeyPairGenerator generator = KeyPairGenerator
                .getInstance(AppConstants.Crypto.PUBLIC_PRIVATE_KEY_ALGORITHM);
            return generator.generateKeyPair();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Cannot generate keypair - algorithm not supported", e);
        }
    }

    /**
     * Helper method to create a valid EventSigningKey for a given event.
     * @param event the event to associate the signing key with
     * @return a new EventSigningKey entity (not yet persisted)
     */
    public static EventSigningKey createSigningKey(Event event)
    {
        KeyPair keyPair = CryptoService.generateKeyPair();
        return new EventSigningKey.Builder()
            .event(event)
            .privateKey(keyPair.getPrivate())
            .publicKey(keyPair.getPublic())
            .build();
    }
}