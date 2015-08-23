/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import org.spongycastle.crypto.params.AsymmetricKeyParameter;

/**
 * A public and private key pair. You might not always have both keys since if you are encrypting a message for someone else, you only have his public key.
 * 
 * <pre>
 * Dependencies:
 * compile 'com.madgag.spongycastle:prov:1.51.0.0'
 * compile 'com.madgag.spongycastle:pkix:1.51.0.0'
 * compile 'com.madgag.spongycastle:pg:1.51.0.0'
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
