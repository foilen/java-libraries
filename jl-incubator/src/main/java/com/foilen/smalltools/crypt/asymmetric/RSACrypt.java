/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import com.foilen.smalltools.Assert;
import com.foilen.smalltools.exception.SmallToolsException;

/**
 * RSA cryptography.
 * 
 * <pre>
 * Default:
 * <ul>
 * <li>RSA: The cipher</li>
 * <li>ECB: Electronic Codebook Mode</li>
 * <li>PKCS1Padding: The padding algorithm</li>
 * <ul>
 * </pre>
 * 
 * Usage:
 * 
 * <pre>
 * // Prepare the message
 * String message = &quot;Hello World&quot;;
 * byte[] data = message.getBytes();
 * 
 * // Encrypt
 * AsymmetricKeys keyPair = crypt.generateKeyPair(2048);
 * byte[] cryptedData = crypt.encrypt(keyPair, data);
 * 
 * // Decrypt
 * byte[] decryptedData = crypt.decrypt(keyPair, cryptedData);
 * </pre>
 */
public class RSACrypt extends AbstractAsymmetricCrypt<RSAKeyDetails> {

    public RSACrypt() {
        super("RSA/ECB/PKCS1Padding", "RSA");
    }

    @Override
    public AsymmetricKeys createKeyPair(RSAKeyDetails keyDetails) {

        BigInteger modulus = keyDetails.getModulus();
        Assert.assertNotNull(modulus, "The modulus must be present");

        AsymmetricKeys asymmetricKeys = new AsymmetricKeys();

        try {

            KeyFactory fact = KeyFactory.getInstance(keyAlgorithm);

            BigInteger publicExponent = keyDetails.getPublicExponent();
            BigInteger privateExponent = keyDetails.getPrivateExponent();
            if (publicExponent != null) {
                RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
                asymmetricKeys.setPublicKey(fact.generatePublic(keySpec));
            }

            if (privateExponent != null) {
                RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, privateExponent);
                asymmetricKeys.setPrivateKey(fact.generatePrivate(keySpec));
            }

            return asymmetricKeys;

        } catch (Exception e) {
            throw new SmallToolsException("Could not create the keys", e);
        }
    }

    @Override
    public RSAKeyDetails retrieveKeyDetails(AsymmetricKeys keyPair) {
        BigInteger modulus = null;
        BigInteger publicExponent = null;
        BigInteger privateExponent = null;

        try {
            KeyFactory fact = KeyFactory.getInstance(keyAlgorithm);

            // Public key
            if (keyPair.getPublicKey() != null) {
                RSAPublicKeySpec pub = fact.getKeySpec(keyPair.getPublicKey(), RSAPublicKeySpec.class);
                publicExponent = pub.getPublicExponent();
                modulus = pub.getModulus();
            }

            // Private key
            if (keyPair.getPrivateKey() != null) {
                RSAPrivateKeySpec priv = fact.getKeySpec(keyPair.getPrivateKey(), RSAPrivateKeySpec.class);
                privateExponent = priv.getPrivateExponent();
                modulus = priv.getModulus();
            }

            return new RSAKeyDetails(modulus, publicExponent, privateExponent);

        } catch (Exception e) {
            throw new SmallToolsException("Could not retrieve the details", e);
        }

    }

}
