/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.symmetric;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.foilen.smalltools.Assert;
import com.foilen.smalltools.crypt.AbstractBufferedBlockCipherCrypt;

/**
 * An abstract class to put all the common methods and properties to use {@link Cipher}. This is for symmetric algorithms.
 * 
 * @param <K>
 *            it is the type of the key details
 */
public abstract class AbstractSymmetricCrypt<K> extends AbstractBufferedBlockCipherCrypt implements SymmetricCrypt<K> {

    protected final String algorithmName;
    protected int ivLength = 0;

    public AbstractSymmetricCrypt(String algorithmName) {
        this.ivLength = getBlockSize();
        this.algorithmName = algorithmName;
    }

    @Override
    public byte[] decrypt(SymmetricKey key, byte[] data) {
        Assert.assertNotNull(key.getKey(), "The key needs to be set to decrypt");
        return decryptWithIV(key.getKey(), data, ivLength);
    }

    @Override
    public byte[] encrypt(SymmetricKey key, byte[] data) {
        Assert.assertNotNull(key.getKey(), "The key needs to be set to encrypt");
        return encryptWithIV(key.getKey(), data);
    }

    @Override
    public SymmetricKey generateKey(int keysize) {
        byte[] keyBytes = new byte[keysize / 8];
        random.nextBytes(keyBytes);
        return new SymmetricKey(new SecretKeySpec(keyBytes, algorithmName));
    }

}
