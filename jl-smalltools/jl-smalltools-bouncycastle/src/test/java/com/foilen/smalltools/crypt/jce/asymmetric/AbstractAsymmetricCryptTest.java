package com.foilen.smalltools.crypt.jce.asymmetric;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

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
     * @param keyPair the key pair
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
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        // Crypt
        AsymmetricKeys keyPair = crypt.generateKeyPair(keySize);
        byte[] cryptedData = crypt.encrypt(keyPair, data);

        // Validate
        Assertions.assertNotNull(cryptedData);
        Assertions.assertNotEquals(data, cryptedData);

        // Decrypt
        byte[] decryptedData = crypt.decrypt(keyPair, cryptedData);

        // Validate
        Assertions.assertNotNull(decryptedData);
        Assertions.assertArrayEquals(data, decryptedData);

        // Recrypt
        byte[] cryptedData2 = crypt.encrypt(keyPair, data);
        Assertions.assertNotEquals(cryptedData2, cryptedData);

        // Decrypt
        decryptedData = crypt.decrypt(keyPair, cryptedData2);

        // Validate
        Assertions.assertNotNull(decryptedData);
        Assertions.assertArrayEquals(data, decryptedData);
    }

    @Test
    public void testGenerateKeyPair() {
        // Generate a first key
        AsymmetricKeys keyPair = crypt.generateKeyPair(keySize);
        Assertions.assertNotNull(keyPair);
        Assertions.assertNotNull(keyPair.getPublicKey());
        Assertions.assertNotNull(keyPair.getPrivateKey());
        Assertions.assertNotEquals(keyPair.getPublicKey(), keyPair.getPrivateKey());

        // Generate a second key
        AsymmetricKeys keyPair2 = crypt.generateKeyPair(keySize);
        Assertions.assertNotNull(keyPair2);
        Assertions.assertNotNull(keyPair2.getPublicKey());
        Assertions.assertNotNull(keyPair2.getPrivateKey());
        Assertions.assertNotEquals(keyPair2.getPublicKey(), keyPair2.getPrivateKey());
        Assertions.assertNotEquals(keyPair2.getPublicKey(), keyPair.getPublicKey());
        Assertions.assertNotEquals(keyPair2.getPrivateKey(), keyPair.getPrivateKey());

        // Check the internals
        assertGeneratedKeyPairInternals(keyPair);
    }

}
