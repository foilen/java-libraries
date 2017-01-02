/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.symmetric;

import javax.crypto.spec.SecretKeySpec;

import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * AESCrypt cryptography. The IV (Initialization Vector) is automatically appended at the beginning of the encrypted data and automatically used by the decrypt method.
 * 
 * <ul>
 * <li>AES: The cipher</li>
 * <li>CBC: Cipher Block Chaining Mode</li>
 * <li>PKCS5Padding: The padding algorithm</li>
 * </ul>
 * 
 * Usage:
 * 
 * <pre>
 * // Prepare the message
 * String message = &quot;Hello World&quot;;
 * byte[] data = message.getBytes();
 * 
 * // Encrypt
 * SymmetricKey key = crypt.generateKey(128);
 * byte[] cryptedData = crypt.encrypt(key, data);
 * 
 * // Decrypt
 * byte[] decryptedData = crypt.decrypt(key, cryptedData);
 * </pre>
 * 
 * <pre>
 * Dependencies:
 * compile 'com.madgag.spongycastle:prov:1.51.0.0'
 * compile 'com.madgag.spongycastle:pkix:1.51.0.0'
 * compile 'com.madgag.spongycastle:pg:1.51.0.0'
 * </pre>
 */
public class AESCrypt extends AbstractSymmetricCrypt<AESKeyDetails> {

    public AESCrypt() {
        super("AES");
    }

    @Override
    public SymmetricKey createKey(AESKeyDetails keyDetails) {

        byte[] theKey = keyDetails.getKey();

        SymmetricKey symmetricKey = new SymmetricKey();

        try {

            if (theKey != null) {
                symmetricKey.setKey(new SecretKeySpec(theKey, algorithmName));
            }

            return symmetricKey;

        } catch (Exception e) {
            throw new SmallToolsException("Could not create the key", e);
        }
    }

    @Override
    protected BufferedBlockCipher generateBufferedBlockCipher() {
        return new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
    }

    @Override
    public AESKeyDetails retrieveKeyDetails(SymmetricKey key) {
        byte[] theKey = null;

        try {

            // Key
            if (key.getKey() != null) {
                theKey = key.getKey().getEncoded();
            }

            return new AESKeyDetails(theKey);

        } catch (Exception e) {
            throw new SmallToolsException("Could not retrieve the details", e);
        }
    }

}
