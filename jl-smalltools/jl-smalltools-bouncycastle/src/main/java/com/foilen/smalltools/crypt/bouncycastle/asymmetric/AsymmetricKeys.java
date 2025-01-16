/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.asymmetric;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * A public and private key pair. You might not always have both keys since if you are encrypting a message for someone else, you only have his public key.
 */
public class AsymmetricKeys {

    private AsymmetricKeyParameter publicKey;
    private AsymmetricKeyParameter privateKey;

    /**
     * Create an empty key pair.
     */
    public AsymmetricKeys() {
    }

    /**
     * Create a key pair and set them.
     *
     * @param publicKey  (optional) the public key
     * @param privateKey (optional) the private key
     */
    public AsymmetricKeys(AsymmetricKeyParameter publicKey, AsymmetricKeyParameter privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * Get the private key.
     *
     * @return the private key
     */
    public AsymmetricKeyParameter getPrivateKey() {
        return privateKey;
    }

    /**
     * Get the public key.
     *
     * @return the public key
     */
    public AsymmetricKeyParameter getPublicKey() {
        return publicKey;
    }

    /**
     * Set the private key.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(AsymmetricKeyParameter privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Set the public key.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(AsymmetricKeyParameter publicKey) {
        this.publicKey = publicKey;
    }

}
