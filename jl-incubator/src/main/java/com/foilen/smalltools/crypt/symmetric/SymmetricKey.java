/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.symmetric;

import javax.crypto.SecretKey;

/**
 * A key for the symmetric algorithms.
 */
public class SymmetricKey {

    private SecretKey key;

    public SymmetricKey() {

    }

    /**
     * Create a key and set it.
     *
     * @param key
     *            (optional) the key
     */
    public SymmetricKey(SecretKey key) {
        this.key = key;
    }

    /**
     * Get the key.
     *
     * @return the key
     */
    public SecretKey getKey() {
        return key;
    }

    /**
     * Set the key.
     *
     * @param key
     *            the key to set
     */
    public void setKey(SecretKey key) {
        this.key = key;
    }

}
