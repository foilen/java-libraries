/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.actions;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import com.foilen.smalltools.StreamsTools;
import com.foilen.smalltools.crypt.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.asymmetric.RSAKeyDetails;
import com.foilen.smalltools.crypt.symmetric.AESCrypt;
import com.foilen.smalltools.crypt.symmetric.AESKeyDetails;
import com.foilen.smalltools.crypt.symmetric.SymmetricKey;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.streampair.StreamPair;
import com.foilen.smalltools.streamwrapper.SymmetricCryptOutputStreamWrapper;
import com.foilen.smalltools.streamwrapper.SymmetricDecryptInputStreamWrapper;

/**
 * Generates a random RSA and AES keys to crypt the streams.
 * 
 * <pre>
 * Defaults:
 * - rsaKeySize = 2048
 * - aesKeySize = 256
 * - negociationTimeoutSeconds = 20
 * </pre>
 * 
 * <pre>
 * Steps:
 * - Generate a random RSA key
 * - Send the public key
 * - Read the remote RSA key
 * - Generate a random AES key
 * - Send RSA.crypt(AES key)
 * - Read the remote AES key
 * - Decrypt the AES key
 * </pre>
 */
public class CryptRsaAesConnectionAction extends AbstractTimeoutConnectionAction {

    private static final int MAX_LENGTH = 1024 * 100;

    private static final RSACrypt rsaCrypt = new RSACrypt();
    private static final AESCrypt aesCrypt = new AESCrypt();

    private int rsaKeySize = 2048;
    private int aesKeySize = 256;

    public CryptRsaAesConnectionAction() {
        negociationTimeoutSeconds = 20;
    }

    public int getAesKeySize() {
        return aesKeySize;
    }

    public int getRsaKeySize() {
        return rsaKeySize;
    }

    /**
     * Choose the size of the AES key.
     * 
     * @param aesKeySize
     *            the size of the key (e.g 128, 192, 256, ...)
     * @return this
     */
    public CryptRsaAesConnectionAction setAesKeySize(int aesKeySize) {
        this.aesKeySize = aesKeySize;
        return this;
    }

    /**
     * Choose the size of the RSA key.
     * 
     * @param rsaKeySize
     *            the size of the key (e.g 1024, 2048, 4096, ...)
     * @return this
     */
    public CryptRsaAesConnectionAction setRsaKeySize(int rsaKeySize) {
        this.rsaKeySize = rsaKeySize;
        return this;
    }

    @Override
    protected Connection wrappedExecuteAction(Connection connection) {

        StreamPair streamPair = connection.getStreamPair();

        // Generate a random RSA key
        AsymmetricKeys localRsaKey = rsaCrypt.generateKeyPair(rsaKeySize);
        RSAKeyDetails localRsaKeyDetails = rsaCrypt.retrieveKeyDetails(localRsaKey);

        // Send the public key and modulus
        OutputStream out = streamPair.getOutputStream();
        StreamsTools.write(out, localRsaKeyDetails.getModulus().toByteArray());
        StreamsTools.write(out, localRsaKeyDetails.getPublicExponent().toByteArray());

        // Read the remote RSA key
        InputStream in = streamPair.getInputStream();
        byte[] remoteModulus = StreamsTools.readBytes(in, MAX_LENGTH);
        byte[] remotePublicExponent = StreamsTools.readBytes(in, MAX_LENGTH);
        RSAKeyDetails remoteRsaKeyDetails = new RSAKeyDetails();
        remoteRsaKeyDetails.setModulus(new BigInteger(remoteModulus));
        remoteRsaKeyDetails.setPublicExponent(new BigInteger(remotePublicExponent));
        AsymmetricKeys remoteRsaKey = rsaCrypt.createKeyPair(remoteRsaKeyDetails);

        // Generate a random AES key
        SymmetricKey localAesKey = aesCrypt.generateKey(aesKeySize);
        AESKeyDetails localAesKeyDetails = aesCrypt.retrieveKeyDetails(localAesKey);

        // Send RSA.crypt(AES key)
        byte[] localAesCryptedKey = rsaCrypt.encrypt(remoteRsaKey, localAesKeyDetails.getKey());
        StreamsTools.write(out, localAesCryptedKey);

        // Send an IV
        byte[] localIv = aesCrypt.generateIV();
        StreamsTools.write(out, localIv);

        // Read the remote AES key
        byte[] remoteAesCryptedKey = StreamsTools.readBytes(in, MAX_LENGTH);

        // Read the IV
        byte[] remoteIv = StreamsTools.readBytes(in, MAX_LENGTH);

        // Decrypt the AES key
        byte[] remoteAesKeyBytes = rsaCrypt.decrypt(localRsaKey, remoteAesCryptedKey);
        AESKeyDetails remoteAesKeyDetails = new AESKeyDetails(remoteAesKeyBytes);
        SymmetricKey remoteAesKey = aesCrypt.createKey(remoteAesKeyDetails);

        // Wrap the streams
        streamPair.setInputStream(new SymmetricDecryptInputStreamWrapper(streamPair.getInputStream(), aesCrypt, localAesKey, localIv));
        streamPair.setOutputStream(new SymmetricCryptOutputStreamWrapper(streamPair.getOutputStream(), aesCrypt, remoteAesKey, remoteIv));

        return connection;
    }
}
