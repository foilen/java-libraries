/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.spongycastle.asymmetric;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.crypto.Cipher;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.FileTools;

/**
 * An abstract class to put all the common methods and properties to use {@link Cipher}. This is for asymmetric algorithms.
 *
 * @param <K>
 *            it is the type of the keys details
 */
public abstract class AbstractAsymmetricCrypt<K> extends AbstractAsymmetricBlockCipherCrypt implements AsymmetricCrypt<K> {

    @Override
    public byte[] decrypt(AsymmetricKeys keyPair, byte[] data) {
        AssertTools.assertNotNull(keyPair.getPrivateKey(), "The private key needs to be set to decrypt");
        return decrypt(keyPair.getPrivateKey(), data);
    }

    @Override
    public byte[] encrypt(AsymmetricKeys keyPair, byte[] data) {
        AssertTools.assertNotNull(keyPair.getPublicKey(), "The public key needs to be set to encrypt");
        return encrypt(keyPair.getPublicKey(), data);
    }

    @Override
    public AsymmetricKeys loadKeysPemFromFile(String fileName) {
        String pem = FileTools.getFileAsString(fileName);
        return loadKeysPemFromString(pem);
    }

    @Override
    public void savePrivateKeyPem(AsymmetricKeys keyPair, String fileName) {
        try {
            savePrivateKeyPem(keyPair, new FileWriter(fileName));
        } catch (IOException e) {
            throw new SmallToolsException("Could not save key", e);
        }
    }

    @Override
    public String savePrivateKeyPemAsString(AsymmetricKeys keyPair) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        savePrivateKeyPem(keyPair, new OutputStreamWriter(result));
        return result.toString();
    }

    @Override
    public String savePublicKeyPemAsString(AsymmetricKeys keyPair) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        savePublicKeyPem(keyPair, new OutputStreamWriter(result));
        return result.toString();
    }

}
