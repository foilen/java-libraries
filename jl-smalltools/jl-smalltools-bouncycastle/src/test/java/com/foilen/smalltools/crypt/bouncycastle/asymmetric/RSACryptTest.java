package com.foilen.smalltools.crypt.bouncycastle.asymmetric;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigInteger;

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
        Assertions.assertNotNull(keyDetails.getModulus());
        Assertions.assertNotNull(keyDetails.getPublicExponent());
        Assertions.assertNotNull(keyDetails.getPrivateExponent());
        Assertions.assertNotEquals(keyDetails.getPublicExponent(), keyDetails.getPrivateExponent());
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
        Assertions.assertEquals(modulus, keyDetails.getModulus());
        Assertions.assertEquals(publicExponent, keyDetails.getPublicExponent());
        Assertions.assertEquals(privateExponent, keyDetails.getPrivateExponent());
    }

    @Test
    public void testSaveAndLoadBothKeys() throws Exception {

        File file = File.createTempFile("junits", null);
        AsymmetricKeys asymmetricKeys = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(asymmetricKeys);

        // Make sure the private key is CRT
        Assertions.assertNotNull(keyDetails.getCrtCoefficient());
        Assertions.assertNotNull(keyDetails.getPrimeExponentP());
        Assertions.assertNotNull(keyDetails.getPrimeExponentQ());
        Assertions.assertNotNull(keyDetails.getPrimeP());
        Assertions.assertNotNull(keyDetails.getPrimeQ());
        Assertions.assertTrue(keyDetails.isCrt());

        // Save
        crypt.saveKeysPem(asymmetricKeys, file.getAbsolutePath());

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromFile(file.getAbsolutePath());
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assertions.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assertions.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assertions.assertEquals(keyDetails.getPrivateExponent(), loadedKeyDetails.getPrivateExponent());
        Assertions.assertEquals(keyDetails.getPublicExponent(), loadedKeyDetails.getPublicExponent());

        // Make sure the private key is CRT
        Assertions.assertEquals(keyDetails.getCrtCoefficient(), loadedKeyDetails.getCrtCoefficient());
        Assertions.assertEquals(keyDetails.getPrimeExponentP(), loadedKeyDetails.getPrimeExponentP());
        Assertions.assertEquals(keyDetails.getPrimeExponentQ(), loadedKeyDetails.getPrimeExponentQ());
        Assertions.assertEquals(keyDetails.getPrimeP(), loadedKeyDetails.getPrimeP());
        Assertions.assertEquals(keyDetails.getPrimeQ(), loadedKeyDetails.getPrimeQ());
        Assertions.assertEquals(keyDetails.isCrt(), loadedKeyDetails.isCrt());
    }

    @Test
    public void testSaveAndLoadBothKeys_SeparateStrings() throws Exception {

        AsymmetricKeys asymmetricKeys = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(asymmetricKeys);

        // Make sure the private key is CRT
        Assertions.assertNotNull(keyDetails.getCrtCoefficient());
        Assertions.assertNotNull(keyDetails.getPrimeExponentP());
        Assertions.assertNotNull(keyDetails.getPrimeExponentQ());
        Assertions.assertNotNull(keyDetails.getPrimeP());
        Assertions.assertNotNull(keyDetails.getPrimeQ());
        Assertions.assertTrue(keyDetails.isCrt());

        // Save
        String privatePem = crypt.savePrivateKeyPemAsString(asymmetricKeys);
        String publicPem = crypt.savePublicKeyPemAsString(asymmetricKeys);
        Assertions.assertTrue(privatePem.contains("RSA PRIVATE KEY"));
        Assertions.assertFalse(privatePem.contains("PUBLIC KEY"));
        Assertions.assertFalse(publicPem.contains("RSA PRIVATE KEY"));
        Assertions.assertTrue(publicPem.contains("PUBLIC KEY"));

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromString(publicPem, privatePem, null, null);
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assertions.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assertions.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assertions.assertEquals(keyDetails.getPrivateExponent(), loadedKeyDetails.getPrivateExponent());
        Assertions.assertEquals(keyDetails.getPublicExponent(), loadedKeyDetails.getPublicExponent());

        // Make sure the private key is CRT
        Assertions.assertEquals(keyDetails.getCrtCoefficient(), loadedKeyDetails.getCrtCoefficient());
        Assertions.assertEquals(keyDetails.getPrimeExponentP(), loadedKeyDetails.getPrimeExponentP());
        Assertions.assertEquals(keyDetails.getPrimeExponentQ(), loadedKeyDetails.getPrimeExponentQ());
        Assertions.assertEquals(keyDetails.getPrimeP(), loadedKeyDetails.getPrimeP());
        Assertions.assertEquals(keyDetails.getPrimeQ(), loadedKeyDetails.getPrimeQ());
        Assertions.assertEquals(keyDetails.isCrt(), loadedKeyDetails.isCrt());
    }

    @Test
    public void testSaveAndLoadPrivateKey() throws Exception {

        File file = File.createTempFile("junits", null);
        AsymmetricKeys asymmetricKeys = crypt.generateKeyPair(keySize);
        RSAKeyDetails keyDetails = crypt.retrieveKeyDetails(asymmetricKeys);

        // Make sure the private key is CRT
        Assertions.assertNotNull(keyDetails.getCrtCoefficient());
        Assertions.assertNotNull(keyDetails.getPrimeExponentP());
        Assertions.assertNotNull(keyDetails.getPrimeExponentQ());
        Assertions.assertNotNull(keyDetails.getPrimeP());
        Assertions.assertNotNull(keyDetails.getPrimeQ());
        Assertions.assertTrue(keyDetails.isCrt());

        // Save
        crypt.savePrivateKeyPem(asymmetricKeys, file.getAbsolutePath());

        // Load
        AsymmetricKeys loadedAsymmetricKeys = crypt.loadKeysPemFromFile(file.getAbsolutePath());
        RSAKeyDetails loadedKeyDetails = crypt.retrieveKeyDetails(loadedAsymmetricKeys);

        Assertions.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assertions.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assertions.assertEquals(keyDetails.getPrivateExponent(), loadedKeyDetails.getPrivateExponent());
        Assertions.assertNotNull(loadedKeyDetails.getPublicExponent());

        // Make sure the private key is CRT
        Assertions.assertEquals(keyDetails.getCrtCoefficient(), loadedKeyDetails.getCrtCoefficient());
        Assertions.assertEquals(keyDetails.getPrimeExponentP(), loadedKeyDetails.getPrimeExponentP());
        Assertions.assertEquals(keyDetails.getPrimeExponentQ(), loadedKeyDetails.getPrimeExponentQ());
        Assertions.assertEquals(keyDetails.getPrimeP(), loadedKeyDetails.getPrimeP());
        Assertions.assertEquals(keyDetails.getPrimeQ(), loadedKeyDetails.getPrimeQ());
        Assertions.assertEquals(keyDetails.isCrt(), loadedKeyDetails.isCrt());
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

        Assertions.assertNotSame(asymmetricKeys, loadedAsymmetricKeys);
        Assertions.assertEquals(keyDetails.getModulus(), loadedKeyDetails.getModulus());
        Assertions.assertNull(loadedKeyDetails.getPrivateExponent());
        Assertions.assertEquals(keyDetails.getPublicExponent(), loadedKeyDetails.getPublicExponent());
    }

}
