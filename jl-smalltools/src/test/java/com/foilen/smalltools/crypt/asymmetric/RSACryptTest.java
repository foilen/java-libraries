/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import java.io.File;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link RSACrypt}.
 */
public class RSACryptTest extends AbstractAsymmetricCryptTest<RSACrypt, RSAKeyDetails> {

    public RSACryptTest() {
        super(2048, new RSACrypt());
    }

    @Override
    protected void assertGeneratedKeyPairInternals(AsymmetricKeys keyPair) {
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(keyPair);
        Assert.assertNotNull(keyDetails.getModulus());
        Assert.assertNotNull(keyDetails.getPublicExponent());
        Assert.assertNotNull(keyDetails.getPrivateExponent());
        Assert.assertNotEquals(keyDetails.getPublicExponent(), keyDetails.getPrivateExponent());
    }

    @Override
    protected void testCreateKeyPairImpl() {
        // Generate a key
        AsymmetricKeys keyPair = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(keyPair);
        BigInteger modulus = keyDetails.getModulus();
        BigInteger publicExponent = keyDetails.getPublicExponent();
        BigInteger privateExponent = keyDetails.getPrivateExponent();

        // Create a new key
        AsymmetricKeys createdKeyPair = crypt.createKeyPair(new RSAKeyDetails(modulus, publicExponent, privateExponent));

        // Validate
        keyDetails = crypt.retrieveKeyDetails(createdKeyPair);
        Assert.assertEquals(modulus, keyDetails.getModulus());
        Assert.assertEquals(publicExponent, keyDetails.getPublicExponent());
        Assert.assertEquals(privateExponent, keyDetails.getPrivateExponent());
    }

    @Test
    public void testSaveAndLoadBothKeys() throws Exception {

        File file = File.createTempFile("junits", null);
        AsymmetricKeys asymmetricKeys = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(asymmetricKeys);

        // Save
        crypt.saveKeysPem(asymmetricKeys, file.getAbsolutePath());

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromFile(file.getAbsolutePath());
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assert.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assert.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assert.assertEquals(keyDetails.getPrivateExponent(), loadedKeyDetails.getPrivateExponent());
        Assert.assertEquals(keyDetails.getPublicExponent(), loadedKeyDetails.getPublicExponent());
    }

    @Test
    public void testSaveAndLoadPrivateKey() throws Exception {

        File file = File.createTempFile("junits", null);
        AsymmetricKeys asymmetricKeys = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(asymmetricKeys);

        // Save
        crypt.savePrivateKeyPem(asymmetricKeys, file.getAbsolutePath());

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromFile(file.getAbsolutePath());
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assert.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assert.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assert.assertEquals(keyDetails.getPrivateExponent(), loadedKeyDetails.getPrivateExponent());
        Assert.assertNull(loadedKeyDetails.getPublicExponent());
    }

    @Test
    public void testSaveAndLoadPublicKey() throws Exception {

        File file = File.createTempFile("junits", null);
        AsymmetricKeys asymmetricKeys = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(asymmetricKeys);

        // Save
        crypt.savePublicKeyPem(asymmetricKeys, file.getAbsolutePath());

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromFile(file.getAbsolutePath());
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assert.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assert.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assert.assertNull(loadedKeyDetails.getPrivateExponent());
        Assert.assertEquals(keyDetails.getPublicExponent(), loadedKeyDetails.getPublicExponent());
    }

}
