/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

/**
 * An interface to define common methods for an Asymmetric cryptographic algorithm.
 * 
 * @param <K>
 *            it is the type of the keys details
 */
public interface AsymmetricCrypt<K> {

    /**
     * Create a key pair from the key details.
     * 
     * @param keyDetails
     *            the key details
     * @return the AsymmetricKeys
     */
    AsymmetricKeys createKeyPair(K keyDetails);

    /**
     * Decrypt the data with the given key.
     * 
     * @param keyPair
     *            the key pair. The private key must be set
     * @param data
     *            the data to decrypt
     * @return the decrypted data
     */
    byte[] decrypt(AsymmetricKeys keyPair, byte[] data);

    /**
     * Encrypt the data with the given key.
     * 
     * @param keyPair
     *            the key pair. The public key must be set
     * @param data
     *            the data to encrypt
     * @return the encrypted data
     */
    byte[] encrypt(AsymmetricKeys keyPair, byte[] data);

    /**
     * Generate a public and private key.
     * 
     * @param keysize
     *            the size of the key (e.g 1024, 2048, 4096, ...)
     * @return the asymmetric key
     */
    AsymmetricKeys generateKeyPair(int keysize);

    /**
     * To retrieve the details of a key pair.
     * 
     * @param keyPair
     *            the pair of keys
     * @return the key details
     */
    K retrieveKeyDetails(AsymmetricKeys keyPair);

}
