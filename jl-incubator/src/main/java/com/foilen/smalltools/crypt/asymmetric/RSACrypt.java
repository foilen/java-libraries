/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import java.math.BigInteger;

import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.generators.RSAKeyPairGenerator;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.RSAKeyGenerationParameters;
import org.spongycastle.crypto.params.RSAKeyParameters;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;

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

    @Override
    public AsymmetricKeys createKeyPair(RSAKeyDetails keyDetails) {

        BigInteger modulus = keyDetails.getModulus();
        AssertTools.assertNotNull(modulus, "The modulus must be present");

        AsymmetricKeys asymmetricKeys = new AsymmetricKeys();

        try {

            BigInteger publicExponent = keyDetails.getPublicExponent();
            BigInteger privateExponent = keyDetails.getPrivateExponent();
            if (publicExponent != null) {
                RSAKeyParameters publicKeyParameters = new RSAKeyParameters(false, modulus, publicExponent);
                asymmetricKeys.setPublicKey(publicKeyParameters);
            }

            if (privateExponent != null) {
                RSAKeyParameters privateKeyParameters = new RSAKeyParameters(true, modulus, privateExponent);
                asymmetricKeys.setPrivateKey(privateKeyParameters);
            }

            return asymmetricKeys;

        } catch (Exception e) {
            throw new SmallToolsException("Could not create the keys", e);
        }
    }

    @Override
    protected AsymmetricBlockCipher generateAsymmetricBlockCipher() {
        return new PKCS1Encoding(new RSAEngine());
    }

    @Override
    public AsymmetricKeys generateKeyPair(int keysize) {
        // Generate
        RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
        KeyGenerationParameters param = new RSAKeyGenerationParameters(new BigInteger("65537"), random, keysize, 80);
        rsaKeyPairGenerator.init(param);
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = rsaKeyPairGenerator.generateKeyPair();

        // Store
        AsymmetricKeys asymmetricKeys = new AsymmetricKeys(asymmetricCipherKeyPair.getPublic(), asymmetricCipherKeyPair.getPrivate());
        return asymmetricKeys;
    }

    @Override
    public RSAKeyDetails retrieveKeyDetails(AsymmetricKeys keyPair) {
        BigInteger modulus = null;
        BigInteger publicExponent = null;
        BigInteger privateExponent = null;

        try {
            // Public key
            if (keyPair.getPublicKey() != null) {
                AsymmetricKeyParameter key = keyPair.getPublicKey();

                if (!(key instanceof RSAKeyParameters)) {
                    throw new SmallToolsException("The public key is not of type RSAKeyParameters. Type is " + key.getClass().getName());
                }

                RSAKeyParameters rsaKey = (RSAKeyParameters) key;
                modulus = rsaKey.getModulus();
                publicExponent = rsaKey.getExponent();
            }

            // Private key
            if (keyPair.getPrivateKey() != null) {
                AsymmetricKeyParameter key = keyPair.getPrivateKey();

                if (!(key instanceof RSAKeyParameters)) {
                    throw new SmallToolsException("The private key is not of type RSAKeyParameters. Type is " + key.getClass().getName());
                }

                RSAKeyParameters rsaKey = (RSAKeyParameters) key;
                modulus = rsaKey.getModulus();
                privateExponent = rsaKey.getExponent();
            }

            return new RSAKeyDetails(modulus, publicExponent, privateExponent);

        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Could not retrieve the details", e);
        }

    }

}
