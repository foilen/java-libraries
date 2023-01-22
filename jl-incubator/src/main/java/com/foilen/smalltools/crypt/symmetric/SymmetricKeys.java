/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.symmetric;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * A public and private key pair. You might not always have both keys since if you are encrypting a message for someone else, you only have his public key.
 */
public class SymmetricKeys {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public SymmetricKeys() {

    }

    /**
     * Create a key pair and set them.
     *
     * @param publicKey
     *            (optional) the public key
     * @param privateKey
     *            (optional) the private key
     */
    public SymmetricKeys(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * @return the privateKey
     */
    public Key getPrivateKey() {
        return privateKey;
    }

    /**
     * @return the publicKey
     */
    public Key getPublicKey() {
        return publicKey;
    }

    /**
     * @param privateKey
     *            the privateKey to set
     */
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * @param publicKey
     *            the publicKey to set
     */
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

}
