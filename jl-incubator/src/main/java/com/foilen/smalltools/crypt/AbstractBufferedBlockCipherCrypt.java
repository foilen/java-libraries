/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import com.foilen.smalltools.crypt.symmetric.SymmetricKey;
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
public abstract class AbstractBufferedBlockCipherCrypt {

    private final static Logger log = LoggerFactory.getLogger(AbstractBufferedBlockCipherCrypt.class);

    protected final SecureRandom random = new SecureRandom();

    /**
     * Get the higher size that is a multiple of the block size.
     *
     * @param length
     *            the minimum size
     * @param addOne
     *            true to add one block (e.g for IV)
     * @return the minimum size + buffer to a multiple of block size
     */
    private int blockSizeMultipleRoof(int length, boolean addOne) {
        int blockSize = getBlockSize();
        int blocCount = length / blockSize;
        int blocCountRest = length % blockSize;

        // Rounding
        if (blocCountRest > 0) {
            ++blocCount;
        }

        // Space for IV
        if (addOne) {
            ++blocCount;
        }

        return blocCount * blockSize;
    }

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
     * Encrypt/Decrypt the data with the specified key.
     *
     * @param key
     *            the key
     * @param ivAndIn
     *            the IV followed by the data to encrypt/decrypt
     * @param ivLength
     *            the amount of bytes in the IV
     * @return the original data
     */
    protected byte[] decryptWithIV(Key key, byte[] ivAndIn, int ivLength) {

        log.debug("decryptWithIV() ivAndIn.length {}", ivAndIn.length);

        try {

            // Separate the iv from the content
            byte[] iv = Arrays.copyOfRange(ivAndIn, 0, ivLength);
            byte[] in = Arrays.copyOfRange(ivAndIn, ivLength, ivAndIn.length);

            // Prepare out
            byte[] out = new byte[in.length];

            // Prepare cipher
            ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(key.getEncoded()), iv);
            BufferedBlockCipher bufferedBlockCipher = generateBufferedBlockCipher();
            bufferedBlockCipher.init(false, parametersWithIV);

            // Decrypt
            int outOff = 0;
            outOff += bufferedBlockCipher.processBytes(in, 0, in.length, out, outOff);
            return finalize(bufferedBlockCipher, out, outOff);

        } catch (Exception e) {
            throw new SmallToolsException("Could not decrypt", e);
        }
    }

    /**
     * Decrypt the data with the specified key.
     *
     * @param key
     *            the key
     * @param iv
     *            the IV to use
     * @param in
     *            the data to decrypt
     * @return the original data
     */
    public byte[] decryptWithIV(SymmetricKey key, byte[] iv, byte[] in) {

        log.debug("decryptWithIV() iv.length {} in.length {}", iv.length, in.length);

        try {

            // Prepare out
            byte[] out = new byte[in.length];

            // Prepare cipher
            ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(key.getKey().getEncoded()), iv);
            BufferedBlockCipher bufferedBlockCipher = generateBufferedBlockCipher();
            bufferedBlockCipher.init(false, parametersWithIV);

            // Decrypt
            int outOff = 0;
            outOff += bufferedBlockCipher.processBytes(in, 0, in.length, out, outOff);
            return finalize(bufferedBlockCipher, out, outOff);

        } catch (Exception e) {
            throw new SmallToolsException("Could not decrypt", e);
        }
    }

    /**
     * Decrypt the data with the specified key.
     *
     * @param key
     *            the key
     * @param iv
     *            the IV to use
     * @param in
     *            the data to decrypt
     * @param from
     *            the start position
     * @param to
     *            the ending position
     * @return the original data
     */
    public byte[] decryptWithIV(SymmetricKey key, byte[] iv, byte[] in, int from, int to) {
        log.debug("decryptWithIV() iv.length {} in.length {} from {} to {}", iv.length, in.length, from, to);
        return processWithIV(key, iv, in, from, to, false);
    }

    /**
     * Encrypt the data with the specified key. Places the IV (initialization vector) before the encrypted text. The IV is securely random.
     *
     * @param key
     *            the key
     * @param in
     *            the data to encrypt
     * @return the IV followed by the encrypted data
     */
    protected byte[] encryptWithIV(Key key, byte[] in) {

        log.debug("encryptWithIV() in.length {}", in.length);

        try {

            // Prepare out
            byte[] out = new byte[blockSizeMultipleRoof(in.length, true)];

            // Prepare cipher
            byte[] iv = generateIV();
            ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(key.getEncoded()), iv);
            BufferedBlockCipher bufferedBlockCipher = generateBufferedBlockCipher();
            bufferedBlockCipher.init(true, parametersWithIV);

            // Crypt
            int outOff = 0;
            outOff += bufferedBlockCipher.processBytes(in, 0, in.length, out, outOff);
            out = finalize(bufferedBlockCipher, out, outOff);
            return concatArrays(iv, out);
        } catch (Exception e) {
            throw new SmallToolsException("Could not encrypt", e);
        }
    }

    /**
     * Encrypt the data with the specified key. Places the IV (initialization vector) before the encrypted text.
     *
     * @param key
     *            the key
     * @param iv
     *            the IV to use
     * @param in
     *            the data to encrypt
     * @return the encrypted data
     */
    public byte[] encryptWithIV(SymmetricKey key, byte[] iv, byte[] in) {

        log.debug("encryptWithIV() iv.length {} in.length {}", iv.length, in.length);

        try {

            // Prepare out
            byte[] out = new byte[blockSizeMultipleRoof(in.length, true)];

            // Prepare cipher
            ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(key.getKey().getEncoded()), iv);
            BufferedBlockCipher bufferedBlockCipher = generateBufferedBlockCipher();
            bufferedBlockCipher.init(true, parametersWithIV);

            // Crypt
            int outOff = 0;
            outOff += bufferedBlockCipher.processBytes(in, 0, in.length, out, outOff);
            return finalize(bufferedBlockCipher, out, outOff);

        } catch (Exception e) {
            throw new SmallToolsException("Could not encrypt", e);
        }
    }

    /**
     * Encrypt the data with the specified key. Places the IV (initialization vector) before the encrypted text.
     *
     * @param key
     *            the key
     * @param iv
     *            the IV to use
     * @param in
     *            the data to encrypt
     * @param from
     *            the start position
     * @param to
     *            the ending position
     * @return the encrypted data
     */
    public byte[] encryptWithIV(SymmetricKey key, byte[] iv, byte[] in, int from, int to) {
        log.debug("encryptWithIV() iv.length {} in.length {} from {} to {}", iv.length, in.length, from, to);
        return processWithIV(key, iv, in, from, to, true);
    }

    private byte[] finalize(BufferedBlockCipher bufferedBlockCipher, byte[] out, int outOff) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        outOff += bufferedBlockCipher.doFinal(out, outOff);
        if (outOff != out.length) {
            out = Arrays.copyOfRange(out, 0, outOff);
        }
        return out;
    }

    protected abstract BufferedBlockCipher generateBufferedBlockCipher();

    /**
     * Generate a secure IV that is the size of a block.
     *
     * @return the IV
     */
    public byte[] generateIV() {
        byte[] iv = new byte[getBlockSize()];
        random.nextBytes(iv);
        return iv;
    }

    /**
     * Tells the block size.
     *
     * @return the block size
     */
    public int getBlockSize() {
        return generateBufferedBlockCipher().getBlockSize();
    }

    /**
     * Encrypt/Decrypt the data with the specified key and IV.
     *
     * @param key
     *            the key
     * @param iv
     *            the IV to use
     * @param in
     *            the data to encrypt/decrypt
     * @param from
     *            the start position
     * @param to
     *            the ending position
     * @param crypt
     *            true to encrypt;false to decrypt
     * @return the encrypted/decrypted data
     */
    private byte[] processWithIV(SymmetricKey key, byte[] iv, byte[] in, int from, int to, boolean crypt) {

        log.debug("processWithIV() crypt {} iv.length {} in.length {} from {} to {}", crypt, iv.length, in.length, from, to);

        try {

            // Prepare out
            int inLength = to - from;
            byte[] out = new byte[blockSizeMultipleRoof(inLength, crypt)];

            // Prepare cipher
            ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(key.getKey().getEncoded()), iv);
            BufferedBlockCipher bufferedBlockCipher = generateBufferedBlockCipher();
            bufferedBlockCipher.init(crypt, parametersWithIV);

            // Crypt
            int outOff = 0;
            outOff += bufferedBlockCipher.processBytes(in, from, inLength, out, outOff);
            return finalize(bufferedBlockCipher, out, outOff);

        } catch (Exception e) {
            throw new SmallToolsException("Could not process", e);
        }
    }

}
