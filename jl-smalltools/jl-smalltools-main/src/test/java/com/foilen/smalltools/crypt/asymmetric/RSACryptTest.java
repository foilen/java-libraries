/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

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

        // Make sure the private key is CRT
        Assert.assertNotNull(keyDetails.getCrtCoefficient());
        Assert.assertNotNull(keyDetails.getPrimeExponentP());
        Assert.assertNotNull(keyDetails.getPrimeExponentQ());
        Assert.assertNotNull(keyDetails.getPrimeP());
        Assert.assertNotNull(keyDetails.getPrimeQ());
        Assert.assertTrue(keyDetails.isCrt());

        // Save
        crypt.saveKeysPem(asymmetricKeys, file.getAbsolutePath());

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromFile(file.getAbsolutePath());
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assert.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assert.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assert.assertEquals(keyDetails.getPrivateExponent(), loadedKeyDetails.getPrivateExponent());
        Assert.assertEquals(keyDetails.getPublicExponent(), loadedKeyDetails.getPublicExponent());

        // Make sure the private key is CRT
        Assert.assertEquals(keyDetails.getCrtCoefficient(), loadedKeyDetails.getCrtCoefficient());
        Assert.assertEquals(keyDetails.getPrimeExponentP(), loadedKeyDetails.getPrimeExponentP());
        Assert.assertEquals(keyDetails.getPrimeExponentQ(), loadedKeyDetails.getPrimeExponentQ());
        Assert.assertEquals(keyDetails.getPrimeP(), loadedKeyDetails.getPrimeP());
        Assert.assertEquals(keyDetails.getPrimeQ(), loadedKeyDetails.getPrimeQ());
        Assert.assertEquals(keyDetails.isCrt(), loadedKeyDetails.isCrt());
    }

    @Test
    public void testSaveAndLoadBothKeys_SeparateStrings() throws Exception {

        AsymmetricKeys asymmetricKeys = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(asymmetricKeys);

        // Make sure the private key is CRT
        Assert.assertNotNull(keyDetails.getCrtCoefficient());
        Assert.assertNotNull(keyDetails.getPrimeExponentP());
        Assert.assertNotNull(keyDetails.getPrimeExponentQ());
        Assert.assertNotNull(keyDetails.getPrimeP());
        Assert.assertNotNull(keyDetails.getPrimeQ());
        Assert.assertTrue(keyDetails.isCrt());

        // Save
        String privatePem = crypt.savePrivateKeyPemAsString(asymmetricKeys);
        String publicPem = crypt.savePublicKeyPemAsString(asymmetricKeys);
        Assert.assertTrue(privatePem.contains("RSA PRIVATE KEY"));
        Assert.assertFalse(privatePem.contains("PUBLIC KEY"));
        Assert.assertFalse(publicPem.contains("RSA PRIVATE KEY"));
        Assert.assertTrue(publicPem.contains("PUBLIC KEY"));

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromString(publicPem, privatePem, null, null);
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assert.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assert.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assert.assertEquals(keyDetails.getPrivateExponent(), loadedKeyDetails.getPrivateExponent());
        Assert.assertEquals(keyDetails.getPublicExponent(), loadedKeyDetails.getPublicExponent());

        // Make sure the private key is CRT
        Assert.assertEquals(keyDetails.getCrtCoefficient(), loadedKeyDetails.getCrtCoefficient());
        Assert.assertEquals(keyDetails.getPrimeExponentP(), loadedKeyDetails.getPrimeExponentP());
        Assert.assertEquals(keyDetails.getPrimeExponentQ(), loadedKeyDetails.getPrimeExponentQ());
        Assert.assertEquals(keyDetails.getPrimeP(), loadedKeyDetails.getPrimeP());
        Assert.assertEquals(keyDetails.getPrimeQ(), loadedKeyDetails.getPrimeQ());
        Assert.assertEquals(keyDetails.isCrt(), loadedKeyDetails.isCrt());
    }

    @Test
    public void testSaveAndLoadPrivateKey() throws Exception {

        File file = File.createTempFile("junits", null);
        AsymmetricKeys asymmetricKeys = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(asymmetricKeys);

        // Make sure the private key is CRT
        Assert.assertNotNull(keyDetails.getCrtCoefficient());
        Assert.assertNotNull(keyDetails.getPrimeExponentP());
        Assert.assertNotNull(keyDetails.getPrimeExponentQ());
        Assert.assertNotNull(keyDetails.getPrimeP());
        Assert.assertNotNull(keyDetails.getPrimeQ());
        Assert.assertTrue(keyDetails.isCrt());

        // Save
        crypt.savePrivateKeyPem(asymmetricKeys, file.getAbsolutePath());

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromFile(file.getAbsolutePath());
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assert.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assert.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assert.assertEquals(keyDetails.getPrivateExponent(), loadedKeyDetails.getPrivateExponent());
        Assert.assertNotNull(loadedKeyDetails.getPublicExponent());

        // Make sure the private key is CRT
        Assert.assertEquals(keyDetails.getCrtCoefficient(), loadedKeyDetails.getCrtCoefficient());
        Assert.assertEquals(keyDetails.getPrimeExponentP(), loadedKeyDetails.getPrimeExponentP());
        Assert.assertEquals(keyDetails.getPrimeExponentQ(), loadedKeyDetails.getPrimeExponentQ());
        Assert.assertEquals(keyDetails.getPrimeP(), loadedKeyDetails.getPrimeP());
        Assert.assertEquals(keyDetails.getPrimeQ(), loadedKeyDetails.getPrimeQ());
        Assert.assertEquals(keyDetails.isCrt(), loadedKeyDetails.isCrt());
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
