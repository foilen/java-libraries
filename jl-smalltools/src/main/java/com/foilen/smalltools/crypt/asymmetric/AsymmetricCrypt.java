/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import java.io.Writer;

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
     * Load the public and/or private keys from a PEM file.
     * 
     * @param fileName
     *            the file name
     * @return the pair of keys
     */
    AsymmetricKeys loadKeysPemFromFile(String fileName);

    /**
     * Load the public and/or private keys from the String.
     * 
     * @param pem
     *            the pem String
     * @return the pair of keys
     */
    AsymmetricKeys loadKeysPemFromString(String pem);

    /**
     * To retrieve the details of a key pair.
     * 
     * @param keyPair
     *            the pair of keys
     * @return the key details
     */
    K retrieveKeyDetails(AsymmetricKeys keyPair);

    /**
     * Save the public and private keys in a PEM file.
     * 
     * @param keyPair
     *            the pair of keys
     * @param fileName
     *            the file name
     */
    void saveKeysPem(AsymmetricKeys keyPair, String fileName);

    /**
     * Save the private key in a PEM file.
     * 
     * @param keyPair
     *            the pair of keys
     * @param fileName
     *            the file name
     */
    void savePrivateKeyPem(AsymmetricKeys keyPair, String fileName);

    /**
     * Save the private key in a PEM writer.
     * 
     * @param keyPair
     *            the pair of keys
     * @param writer
     *            the writer. Will be closed at the end
     */
    void savePrivateKeyPem(AsymmetricKeys keyPair, Writer writer);

    /**
     * Save the private key in a PEM String.
     * 
     * @param keyPair
     *            the pair of keys
     */
    String savePrivateKeyPemAsString(AsymmetricKeys keyPair);

    /**
     * Save the public key in a PEM file.
     * 
     * @param keyPair
     *            the pair of keys
     * @param fileName
     *            the file name
     */
    void savePublicKeyPem(AsymmetricKeys keyPair, String fileName);

    /**
     * Save the public key in a PEM writer.
     * 
     * @param keyPair
     *            the pair of keys
     * @param writer
     *            the writer. Will be closed at the end
     */
    void savePublicKeyPem(AsymmetricKeys keyPair, Writer writer);

    /**
     * Save the public key in a PEM String.
     * 
     * @param keyPair
     *            the pair of keys
     */
    String savePublicKeyPemAsString(AsymmetricKeys keyPair);

}
