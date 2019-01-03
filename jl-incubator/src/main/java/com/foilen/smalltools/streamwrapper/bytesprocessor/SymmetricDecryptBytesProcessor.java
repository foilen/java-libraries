/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper.bytesprocessor;

import java.util.Arrays;

import com.foilen.smalltools.crypt.symmetric.AbstractSymmetricCrypt;
import com.foilen.smalltools.crypt.symmetric.SymmetricKey;

/**
 * A bytes processor that decrypts the data with a symmetric key. The IV will be updated with the latest ciphertext block.
 */
public class SymmetricDecryptBytesProcessor extends AbstractBytesProcessor {

    private AbstractSymmetricCrypt<?> crypt;
    private SymmetricKey key;
    private byte[] iv;
    private int ivLength;

    public SymmetricDecryptBytesProcessor(AbstractSymmetricCrypt<?> crypt, SymmetricKey key, byte[] iv) {
        this.crypt = crypt;
        this.key = key;
        this.iv = iv;
        ivLength = iv.length;
    }

    @Override
    public byte[] process(byte[] content, int offset, int length) {
        // Decrypt
        byte[] result = crypt.decryptWithIV(key, iv, content, offset, length);

        // Update the IV with the last cipherblock
        if (length == ivLength) {
            if (offset == 0) {
                iv = Arrays.copyOf(content, ivLength);
            } else {
                iv = Arrays.copyOfRange(content, offset, offset + length + 1);
            }
        } else {
            iv = Arrays.copyOfRange(content, offset + length - ivLength, offset + length);
        }

        return result;
    }

}
