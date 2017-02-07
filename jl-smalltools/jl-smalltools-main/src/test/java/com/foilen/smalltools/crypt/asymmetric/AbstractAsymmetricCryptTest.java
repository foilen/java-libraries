/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.asymmetric;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for any {@link AsymmetricCrypt}.
 */
public abstract class AbstractAsymmetricCryptTest<T extends AsymmetricCrypt<K>, K> {

    protected int keySize = 1024;
    protected T crypt;

    public AbstractAsymmetricCryptTest(int keySize, T crypt) {
        this.keySize = keySize;
        this.crypt = crypt;
    }

    /**
     * Assert the internal properties of the key pair.
     *
     * @param keyPair
     *            the key pair
     */
    protected abstract void assertGeneratedKeyPairInternals(AsymmetricKeys keyPair);

    @Test
    public void testCreateKeyPair() {
        testCreateKeyPairImpl();
    }

    /**
     * Do the test for the {@link AsymmetricCrypt#createKeyPair(Object)} method.
     */
    protected abstract void testCreateKeyPairImpl();

    @Test
    public void testEncryptAndDecrypt() {
        // Prepare the message
        String message = "Hello World";
        byte[] data = message.getBytes();

        // Crypt
        AsymmetricKeys keyPair = crypt.generateKeyPair(keySize);
        byte[] cryptedData = crypt.encrypt(keyPair, data);

        // Validate
        Assert.assertNotNull(cryptedData);
        Assert.assertThat(cryptedData, IsNot.not(IsEqual.equalTo(data)));

        // Decrypt
        byte[] decryptedData = crypt.decrypt(keyPair, cryptedData);

        // Validate
        Assert.assertNotNull(decryptedData);
        Assert.assertArrayEquals(data, decryptedData);

        // Recrypt
        byte[] cryptedData2 = crypt.encrypt(keyPair, data);
        Assert.assertThat(cryptedData, IsNot.not(IsEqual.equalTo(cryptedData2)));

        // Decrypt
        decryptedData = crypt.decrypt(keyPair, cryptedData2);

        // Validate
        Assert.assertNotNull(decryptedData);
        Assert.assertArrayEquals(data, decryptedData);
    }

    @Test
    public void testGenerateKeyPair() {
        // Generate a first key
        AsymmetricKeys keyPair = crypt.generateKeyPair(keySize);
        Assert.assertNotNull(keyPair);
        Assert.assertNotNull(keyPair.getPublicKey());
        Assert.assertNotNull(keyPair.getPrivateKey());
        Assert.assertNotEquals(keyPair.getPublicKey(), keyPair.getPrivateKey());

        // Generate a second key
        AsymmetricKeys keyPair2 = crypt.generateKeyPair(keySize);
        Assert.assertNotNull(keyPair2);
        Assert.assertNotNull(keyPair2.getPublicKey());
        Assert.assertNotNull(keyPair2.getPrivateKey());
        Assert.assertNotEquals(keyPair2.getPublicKey(), keyPair2.getPrivateKey());
        Assert.assertNotEquals(keyPair2.getPublicKey(), keyPair.getPublicKey());
        Assert.assertNotEquals(keyPair2.getPrivateKey(), keyPair.getPrivateKey());

        // Check the internals
        assertGeneratedKeyPairInternals(keyPair);
    }

}
