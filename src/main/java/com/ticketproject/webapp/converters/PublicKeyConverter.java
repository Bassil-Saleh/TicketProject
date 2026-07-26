package com.ticketproject.webapp.converters;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import com.ticketproject.webapp.constants.AppConstants;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * PublicKeyConverter is used to handle conversion of PublicKey objects
 * into byte[] before getting stored into the database, as well as
 * converting byte[] from the database back into PublicKey objects.
 */
@Converter
public class PublicKeyConverter implements AttributeConverter<PublicKey, byte[]>
{

    @Override
    public byte[] convertToDatabaseColumn(PublicKey publicKey)
    {
        if (publicKey == null)
            return null;
        return publicKey.getEncoded();
    }

    @Override
    public PublicKey convertToEntityAttribute(byte[] dbData)
    {
        if (dbData == null || dbData.length == 0)
            return null;
        try
        {
            KeyFactory keyFactory = KeyFactory
                .getInstance(AppConstants.Crypto.PUBLIC_PRIVATE_KEY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(dbData));
        }
        catch (InvalidKeySpecException e)
        {
            throw new RuntimeException("Cannot create PublicKey - invalid key specification", e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Cannot create PublicKey - algorithm not supported", e);
        }
    }
}
