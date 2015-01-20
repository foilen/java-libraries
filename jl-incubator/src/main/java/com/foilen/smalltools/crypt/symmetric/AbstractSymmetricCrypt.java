/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.symmetric;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.foilen.smalltools.Assert;
import com.foilen.smalltools.crypt.AbstractCrypt;
import com.foilen.smalltools.exception.SmallToolsException;

/**
 * An abstract class to put all the common methods and properties to use {@link Cipher}. This is for symmetric algorithms.
 * 
 * @param <K>
 *            it is the type of the key details
 */
public abstract class AbstractSymmetricCrypt<K> extends AbstractCrypt implements SymmetricCrypt<K> {

    protected int ivLength = 0;

    /**
     * Use a cipher without IV.
     * 
     * @param transformation
     * @param keyAlgorithm
     * @param useIv
     *            true to use IV appended at the beginning of the encrypted data
     */
    public AbstractSymmetricCrypt(String transformation, String keyAlgorithm, boolean useIv) {
        super(transformation, keyAlgorithm);
        this.ivLength = getBlockSize();
    }

    @Override
    public byte[] decrypt(SymmetricKey key, byte[] data) {
        Assert.assertNotNull(key.getKey(), "The key needs to be set to decrypt");
        if (ivLength == 0) {
            return decrypt(key.getKey(), data);
        } else {
            return decryptWithIV(key.getKey(), data, ivLength);
        }
    }

    @Override
    public byte[] encrypt(SymmetricKey key, byte[] data) {
        Assert.assertNotNull(key.getKey(), "The key needs to be set to encrypt");
        if (ivLength == 0) {
            return encrypt(key.getKey(), data);
        } else {
            return encryptWithIV(key.getKey(), data);
        }
    }

    @Override
    public SymmetricKey generateKey(int keysize) {
        try {

            KeyGenerator kg = KeyGenerator.getInstance(keyAlgorithm);
            kg.init(keysize);
            SecretKey secretKey = kg.generateKey();
            return new SymmetricKey(secretKey);

        } catch (NoSuchAlgorithmException e) {
            throw new SmallToolsException("Could not generate the keys", e);
        }
    }

}
