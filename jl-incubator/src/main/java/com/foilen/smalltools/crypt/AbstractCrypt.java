/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import com.foilen.smalltools.crypt.symmetric.SymmetricKey;
import com.foilen.smalltools.exception.SmallToolsException;

/**
 * An abstract class to put all the common methods and properties to use {@link Cipher}. This is for symmetric and asymmetric algorithms.
 */
public abstract class AbstractCrypt {

    protected String keyAlgorithm;
    protected String cipherTransformation;

    public AbstractCrypt(String transformation, String keyAlgorithm) {
        this.cipherTransformation = transformation;
        this.keyAlgorithm = keyAlgorithm;
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
     * Decrypt the data with the specified key.
     * 
     * @param key
     *            the key
     * @param data
     *            the data to decrypt
     * @return the original data
     */
    protected byte[] decrypt(Key key, byte[] data) {

        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cipherData = cipher.doFinal(data);
            return cipherData;

        } catch (Exception e) {
            throw new SmallToolsException("Could not decrypt", e);
        }
    }

    /**
     * Decrypt the data with the specified key.
     * 
     * @param key
     *            the key
     * @param data
     *            the IV followed by the data to decrypt
     * @param ivLength
     *            the amount of bytes in the IV
     * @return the original data
     */
    protected byte[] decryptWithIV(Key key, byte[] data, int ivLength) {

        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(data, 0, ivLength));
            byte[] cipherData = cipher.doFinal(data, ivLength, data.length - ivLength);
            return cipherData;

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
     * @param data
     *            the data to decrypt
     * @return the original data
     */
    public byte[] decryptWithIV(SymmetricKey key, byte[] iv, byte[] data) {

        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.DECRYPT_MODE, key.getKey(), new IvParameterSpec(iv));
            return cipher.doFinal(data);

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
     * @param data
     *            the data to decrypt
     * @param offset
     * @param length
     * @return the original data
     */
    public byte[] decryptWithIV(SymmetricKey key, byte[] iv, byte[] data, int offset, int length) {

        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.DECRYPT_MODE, key.getKey(), new IvParameterSpec(iv));
            return cipher.doFinal(data, offset, length);

        } catch (Exception e) {
            throw new SmallToolsException("Could not decrypt", e);
        }
    }

    /**
     * Encrypt the data with the specified key.
     * 
     * @param key
     *            the key
     * @param data
     *            the data to encrypt
     * @return the encrypted data
     */
    protected byte[] encrypt(Key key, byte[] data) {

        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherData = cipher.doFinal(data);
            return cipherData;

        } catch (Exception e) {
            throw new SmallToolsException("Could not encrypt", e);
        }
    }

    /**
     * Encrypt the data with the specified key. Places the IV (initialization vector) before the encrypted text. The IV is securely random.
     * 
     * @param key
     *            the key
     * @param data
     *            the data to encrypt
     * @return the IV followed by the encrypted data
     */
    protected byte[] encryptWithIV(Key key, byte[] data) {

        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();
            byte[] cipherData = cipher.doFinal(data);
            return concatArrays(iv, cipherData);

        } catch (Exception e) {
            throw new SmallToolsException("Could not encrypt", e);
        }
    }

    /**
     * Encrypt the data with the specified key. Places the IV (initialization vector) before the encrypted text. The IV is securely random.
     * 
     * @param key
     *            the key
     * @param iv
     *            the IV to use
     * @param data
     *            the data to encrypt
     * @return the encrypted data
     */
    public byte[] encryptWithIV(SymmetricKey key, byte[] iv, byte[] data) {

        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.ENCRYPT_MODE, key.getKey(), new IvParameterSpec(iv));
            return cipher.doFinal(data);

        } catch (Exception e) {
            throw new SmallToolsException("Could not encrypt", e);
        }
    }

    /**
     * Encrypt the data with the specified key. Places the IV (initialization vector) before the encrypted text. The IV is securely random.
     * 
     * @param key
     *            the key
     * @param iv
     *            the IV to use
     * @param data
     *            the data to encrypt
     * @param offset
     * @param length
     * @return the encrypted data
     */
    public byte[] encryptWithIV(SymmetricKey key, byte[] iv, byte[] data, int offset, int length) {

        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.ENCRYPT_MODE, key.getKey(), new IvParameterSpec(iv));
            return cipher.doFinal(data, offset, length);

        } catch (Exception e) {
            throw new SmallToolsException("Could not encrypt", e);
        }
    }

    /**
     * Generate a secure IV that is the size of a block.
     * 
     * @return the IV
     */
    public byte[] generateIV() {

        SecureRandom random = new SecureRandom();
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
        try {

            Cipher cipher = Cipher.getInstance(cipherTransformation);
            return cipher.getBlockSize();

        } catch (Exception e) {
            throw new SmallToolsException("Could not find the cipher", e);
        }
    }

    /**
     * Get the transformation parameters that is used by the {@link Cipher}.
     * 
     * @return the cipherTransformation
     */
    public String getCipherTransformation() {
        return cipherTransformation;
    }

    /**
     * The algorithm to manage the key.
     * 
     * @return the keyAlgorithm
     */
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    /**
     * Set the transformation parameters that is used by the {@link Cipher}.
     * 
     * @param cipherTransformation
     *            the cipherTransformation to set
     */
    public void setCipherTransformation(String cipherTransformation) {
        this.cipherTransformation = cipherTransformation;
    }

    /**
     * The algorithm to manage the key.
     * 
     * @param keyAlgorithm
     *            the keyAlgorithm to set
     */
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }
}
