/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.symmetric;

/**
 * Contains the details of the AES key. Given by the method {@link AESCrypt#retrieveKeyDetails(SymmetricKey)}.
 */
public class AESKeyDetails {

    private byte[] key;

    public AESKeyDetails() {
    }

    public AESKeyDetails(byte[] key) {
        this.key = key;
    }

    /**
     * @return the key
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(byte[] key) {
        this.key = key;
    }

}
