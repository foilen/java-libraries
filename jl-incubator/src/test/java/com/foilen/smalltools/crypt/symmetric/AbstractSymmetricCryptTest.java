/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.symmetric;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for any {@link SymmetricCrypt}.
 */
public abstract class AbstractSymmetricCryptTest<T extends SymmetricCrypt<K>, K> {

    protected int keySize = 256;
    protected T crypt;

    public AbstractSymmetricCryptTest(int keySize, T crypt) {
        this.keySize = keySize;
        this.crypt = crypt;
    }

    /**
     * Assert the internal properties of the key.
     *
     * @param key
     *            the key pair
     */
    protected abstract void assertGeneratedKeyPairInternals(SymmetricKey key);

    @Test
    public void testCreateKeyPair() {
        testCreateKeyPairImpl();
    }

    /**
     * Do the test for the {@link SymmetricCrypt#createKey(Object)} method.
     */
    protected abstract void testCreateKeyPairImpl();

    @Test
    public void testEncryptAndDecrypt() {
        // Prepare the message
        String message = "Hello World";
        byte[] data = message.getBytes();

        // Crypt
        SymmetricKey key = crypt.generateKey(keySize);
        byte[] cryptedData = crypt.encrypt(key, data);

        // Validate
        Assert.assertNotNull(cryptedData);
        Assert.assertThat(cryptedData, IsNot.not(IsEqual.equalTo(data)));

        // Encrypt
        byte[] decryptedData = crypt.decrypt(key, cryptedData);

        // Validate
        Assert.assertNotNull(decryptedData);
        Assert.assertArrayEquals(data, decryptedData);

        // Recrypt
        byte[] cryptedData2 = crypt.encrypt(key, data);
        Assert.assertThat(cryptedData, IsNot.not(IsEqual.equalTo(cryptedData2)));

        // Decrypt
        decryptedData = crypt.decrypt(key, cryptedData2);

        // Validate
        Assert.assertNotNull(decryptedData);
        Assert.assertArrayEquals(data, decryptedData);
    }

    @Test
    public void testGenerateKeyPair() {
        // Generate a first key
        SymmetricKey key = crypt.generateKey(keySize);
        Assert.assertNotNull(key);
        Assert.assertNotNull(key.getKey());

        // Generate a second key
        SymmetricKey key2 = crypt.generateKey(keySize);
        Assert.assertNotNull(key2);
        Assert.assertNotNull(key2.getKey());
        Assert.assertNotEquals(key2.getKey(), key.getKey());

        // Check the internals
        assertGeneratedKeyPairInternals(key);
    }

}
