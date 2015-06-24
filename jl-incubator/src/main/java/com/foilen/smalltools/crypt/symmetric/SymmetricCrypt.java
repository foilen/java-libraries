/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.symmetric;

/**
 * An interface to define common methods for an Asymmetric cryptographic algorithm.
 * 
 * @param <K>
 *            it is the type of the key details
 */
public interface SymmetricCrypt<K> {

    /**
     * Create a key from the key details.
     * 
     * @param keyDetails
     *            the key details
     * @return the key
     */
    SymmetricKey createKey(K keyDetails);

    /**
     * Decrypt the data with the given key.
     * 
     * @param key
     *            the key
     * @param data
     *            the data to decrypt
     * @return the decrypted data
     */
    byte[] decrypt(SymmetricKey key, byte[] data);

    /**
     * Encrypt the data with the given key.
     * 
     * @param key
     *            the key
     * @param data
     *            the data to encrypt
     * @return the encrypted data
     */
    byte[] encrypt(SymmetricKey key, byte[] data);

    /**
     * Generate a key.
     * 
     * @param keysize
     *            the size of the key (e.g 128, 192, 256, ...)
     * @return the symmetric key
     */
    SymmetricKey generateKey(int keysize);

    /**
     * To retrieve the details of a key.
     * 
     * @param key
     *            the key
     * @return the key details
     */
    K retrieveKeyDetails(SymmetricKey key);

}
