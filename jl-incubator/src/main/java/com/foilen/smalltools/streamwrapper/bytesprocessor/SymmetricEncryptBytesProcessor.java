/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper.bytesprocessor;

import java.util.Arrays;

import com.foilen.smalltools.crypt.symmetric.AbstractSymmetricCrypt;
import com.foilen.smalltools.crypt.symmetric.SymmetricKey;

/**
 * A bytes processor that encrypts the data with a symmetric key. The IV will be updated with the latest ciphertext block.
 */
public class SymmetricEncryptBytesProcessor extends AbstractBytesProcessor {

    private AbstractSymmetricCrypt<?> crypt;
    private SymmetricKey key;
    private byte[] iv;
    private int ivLength;

    public SymmetricEncryptBytesProcessor(AbstractSymmetricCrypt<?> crypt, SymmetricKey key, byte[] iv) {
        this.crypt = crypt;
        this.key = key;
        this.iv = iv;
        ivLength = iv.length;
    }

    @Override
    public byte[] process(byte[] content, int offset, int length) {
        // Encrypt
        byte[] result = crypt.encryptWithIV(key, iv, content, offset, length);

        // Update the IV
        if (result.length == ivLength) {
            iv = Arrays.copyOf(result, ivLength);
        } else {
            iv = Arrays.copyOfRange(result, result.length - ivLength, result.length);
        }

        return result;
    }

}
