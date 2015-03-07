/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import javax.crypto.Cipher;

import com.foilen.smalltools.crypt.AbstractAsymmetricBlockCipherCrypt;
import com.foilen.smalltools.tools.AssertTools;

/**
 * An abstract class to put all the common methods and properties to use {@link Cipher}. This is for asymmetric algorithms.
 * 
 * @param <K>
 *            it is the type of the keys details
 */
public abstract class AbstractAsymmetricCrypt<K> extends AbstractAsymmetricBlockCipherCrypt implements AsymmetricCrypt<K> {

    @Override
    public byte[] decrypt(AsymmetricKeys keyPair, byte[] data) {
        AssertTools.assertNotNull(keyPair.getPrivateKey(), "The private key needs to be set to decrypt");
        return decrypt(keyPair.getPrivateKey(), data);
    }

    @Override
    public byte[] encrypt(AsymmetricKeys keyPair, byte[] data) {
        AssertTools.assertNotNull(keyPair.getPublicKey(), "The public key needs to be set to encrypt");
        return encrypt(keyPair.getPublicKey(), data);
    }

}
