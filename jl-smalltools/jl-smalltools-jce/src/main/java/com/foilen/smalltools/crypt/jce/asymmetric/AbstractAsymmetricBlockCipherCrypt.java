package com.foilen.smalltools.crypt.jce.asymmetric;

import com.foilen.smalltools.exception.SmallToolsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.SecureRandom;

/**
 * An abstract class to put all the common methods and properties to use {@link Cipher}. This is for asymmetric algorithms.
 */
public abstract class AbstractAsymmetricBlockCipherCrypt {

    private final static Logger log = LoggerFactory.getLogger(AbstractAsymmetricBlockCipherCrypt.class);

    /**
     * The random generator.
     */
    protected final SecureRandom random = new SecureRandom();

    /**
     * Create an array that contains all the contents of the arrays.
     *
     * @param arrays the arrays to concatenate
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
     * @param key the key
     * @param in  the data to decrypt
     * @return the original data
     */
    protected byte[] decrypt(Key key, byte[] in) {
        log.debug("decrypt() in.length {}", in.length);
        return process(key, in, Cipher.DECRYPT_MODE);
    }

    /**
     * Encrypt the data with the specified key.
     *
     * @param key the key
     * @param in  the data to encrypt
     * @return the encrypted data
     */
    protected byte[] encrypt(Key key, byte[] in) {
        log.debug("encrypt() in.length {}", in.length);
        return process(key, in, Cipher.ENCRYPT_MODE);
    }

    /**
     * Returns the cipher transformation string (e.g. "RSA/ECB/PKCS1Padding").
     *
     * @return the transformation
     */
    protected abstract String getCipherTransformation();

    /**
     * Encrypt/Decrypt the data with the specified key.
     *
     * @param key        the key
     * @param in         the data to encrypt/decrypt
     * @param cipherMode {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}
     * @return the encrypted/decrypted data
     */
    private byte[] process(Key key, byte[] in, int cipherMode) {

        log.debug("process() cipherMode {} in.length {}", cipherMode, in.length);

        try {
            Cipher cipher = Cipher.getInstance(getCipherTransformation());
            cipher.init(cipherMode, key, random);
            return cipher.doFinal(in);
        } catch (Exception e) {
            throw new SmallToolsException("Could not process", e);
        }
    }

}
