/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.asymmetric;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * A public and private key pair. You might not always have both keys since if you are encrypting a message for someone else, you only have his public key.
 *
 * <pre>
 * Dependencies:
 * compile 'org.bouncycastle:bcpkix-jdk15on:1.58'
 * compile 'org.bouncycastle:bcpg-jdk15on:1.58'
 * compile 'org.bouncycastle:bcprov-jdk15on:1.58'
 * </pre>
 */
public class AsymmetricKeys {

    private AsymmetricKeyParameter publicKey;
    private AsymmetricKeyParameter privateKey;

    public AsymmetricKeys() {
    }

    /**
     * Create a key pair and set them.
     *
     * @param publicKey
     *            (optional) the public key
     * @param privateKey
     *            (optional) the private key
     */
    public AsymmetricKeys(AsymmetricKeyParameter publicKey, AsymmetricKeyParameter privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public AsymmetricKeyParameter getPrivateKey() {
        return privateKey;
    }

    public AsymmetricKeyParameter getPublicKey() {
        return publicKey;
    }

    public void setPrivateKey(AsymmetricKeyParameter privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(AsymmetricKeyParameter publicKey) {
        this.publicKey = publicKey;
    }

}
