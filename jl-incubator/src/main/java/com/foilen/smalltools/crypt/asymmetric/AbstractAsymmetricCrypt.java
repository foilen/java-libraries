/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import com.foilen.smalltools.Assert;
import com.foilen.smalltools.crypt.AbstractCrypt;
import com.foilen.smalltools.exception.SmallToolsException;

/**
 * An abstract class to put all the common methods and properties to use {@link Cipher}. This is for asymmetric algorithms.
 * 
 * @param <K>
 *            it is the type of the keys details
 */
public abstract class AbstractAsymmetricCrypt<K> extends AbstractCrypt implements AsymmetricCrypt<K> {

    public AbstractAsymmetricCrypt(String transformation, String keyAlgorithm) {
        super(transformation, keyAlgorithm);
    }

    @Override
    public byte[] decrypt(AsymmetricKeys keyPair, byte[] data) {
        Assert.assertNotNull(keyPair.getPrivateKey(), "The private key needs to be set to decrypt");
        return decrypt(keyPair.getPrivateKey(), data);
    }

    @Override
    public byte[] encrypt(AsymmetricKeys keyPair, byte[] data) {
        Assert.assertNotNull(keyPair.getPublicKey(), "The public key needs to be set to encrypt");
        return encrypt(keyPair.getPublicKey(), data);
    }

    @Override
    public AsymmetricKeys generateKeyPair(int keysize) {
        try {

            KeyPairGenerator kpg = KeyPairGenerator.getInstance(keyAlgorithm);
            kpg.initialize(keysize);
            KeyPair kp = kpg.genKeyPair();
            return new AsymmetricKeys(kp.getPublic(), kp.getPrivate());

        } catch (NoSuchAlgorithmException e) {
            throw new SmallToolsException("Could not generate the keys", e);
        }
    }

}
