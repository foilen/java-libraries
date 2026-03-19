package com.foilen.smalltools.crypt.jce.asymmetric;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * A public and private key pair. You might not always have both keys since if you are encrypting a message for someone else, you only have his public key.
 */
public class AsymmetricKeys {

    private PublicKey publicKey;
    private PrivateKey privateKey;

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
    public AsymmetricKeys(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * Get the private key.
     *
     * @return the private key
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Get the public key.
     *
     * @return the public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Set the private key.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Set the public key.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

}
