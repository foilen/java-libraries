/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import java.math.BigInteger;

import org.junit.Assert;

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

}
