/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.asymmetric;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * An abstract class to put all the common methods and properties to use {@link BufferedBlockCipher}. This is for symmetric and asymmetric algorithms.
 * 
 * <pre>
 * Dependencies:
 * compile 'com.madgag.spongycastle:prov:1.51.0.0'
 * compile 'com.madgag.spongycastle:pkix:1.51.0.0'
 * compile 'com.madgag.spongycastle:pg:1.51.0.0'
 * </pre>
 */
public abstract class AbstractAsymmetricBlockCipherCrypt {

    private final static Logger log = LoggerFactory.getLogger(AbstractAsymmetricBlockCipherCrypt.class);

    protected final SecureRandom random = new SecureRandom();

    /**
     * Create an array that contains all the contents of the arrays.
     * 
     * @param arrays
     *            the arrays to concatenate
     * @return the final array
     */
    protected byte[] concatArrays(byte[]... arrays) {

        // Check the length
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        // Create the empty array
        byte[] concatenated = new byte[totalLength];

        // Copy all
        int concatPos = 0;
        for (byte[] array : arrays) {
            for (int i = 0; i < array.length; ++i) {
                concatenated[concatPos++] = array[i];
            }
        }

        return concatenated;
    }

    /**
     * Decrypt the data with the specified key.
     * 
     * @param key
     *            the key
     * @param in
     *            the data to decrypt
     * @return the original data
     */
    protected byte[] decrypt(AsymmetricKeyParameter key, byte[] in) {
        log.debug("decrypt() in.length {}", in.length);
        return process(key, in, false);
    }

    /**
     * Encrypt the data with the specified key.
     * 
     * @param key
     *            the key
     * @param in
     *            the data to encrypt
     * @return the encrypted data
     */
    protected byte[] encrypt(AsymmetricKeyParameter key, byte[] in) {
        log.debug("encrypt() in.length {}", in.length);
        return process(key, in, true);
    }

    protected abstract AsymmetricBlockCipher generateAsymmetricBlockCipher();

    /**
     * Tells the block size.
     * 
     * @return the block size
     */
    public int getBlockSize() {
        return generateAsymmetricBlockCipher().getInputBlockSize();
    }

    /**
     * Encrypt/Decrypt the data with the specified key.
     * 
     * @param key
     *            the key
     * @param in
     *            the data to encrypt/decrypt
     * @param crypt
     *            true to encrypt;false to decrypt
     * @return the encrypted/decrypted data
     */
    private byte[] process(AsymmetricKeyParameter key, byte[] in, boolean crypt) {

        log.debug("process() crypt {} in.length {}", crypt, in.length);

        try {

            // Prepare cipher
            AsymmetricBlockCipher asymmetricBlockCipher = generateAsymmetricBlockCipher();
            asymmetricBlockCipher.init(crypt, key);

            // Process
            return asymmetricBlockCipher.processBlock(in, 0, in.length);

        } catch (Exception e) {
            throw new SmallToolsException("Could not process", e);
        }
    }

}