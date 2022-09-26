/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.spongycastle.asymmetric;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.spongycastle.asn1.pkcs.RSAPrivateKey;
import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.generators.RSAKeyPairGenerator;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.RSAKeyGenerationParameters;
import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.spongycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemObjectGenerator;
import org.spongycastle.util.io.pem.PemReader;
import org.spongycastle.util.io.pem.PemWriter;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.CollectionsTools;

/**
 * RSA cryptography.
 *
 * Default:
 * <ul>
 * <li>RSA: The cipher</li>
 * <li>ECB: Electronic Codebook Mode</li>
 * <li>PKCS1Padding: The padding algorithm</li>
 * </ul>
 *
 * Usage:
 *
 * <pre>
 * // Prepare the message
 * String message = &quot;Hello World&quot;;
 * byte[] data = message.getBytes(CharsetTools.UTF_8);
 *
 * // Encrypt
 * AsymmetricKeys keyPair = crypt.generateKeyPair(2048);
 * byte[] cryptedData = crypt.encrypt(keyPair, data);
 *
 * // Decrypt
 * byte[] decryptedData = crypt.decrypt(keyPair, cryptedData);
 * </pre>
 */
public class RSACrypt extends AbstractAsymmetricCrypt<RSAKeyDetails> {

    public static final RSACrypt RSA_CRYPT = new RSACrypt();

    @Override
    public AsymmetricKeys createKeyPair(RSAKeyDetails keyDetails) {

        if (keyDetails.getModulus() == null && keyDetails.getPrivateExponent() == null && keyDetails.getPublicExponent() == null) {
            return null;
        }

        BigInteger modulus = keyDetails.getModulus();
        AssertTools.assertNotNull(modulus, "The modulus must be present");

        AsymmetricKeys asymmetricKeys = new AsymmetricKeys();

        try {

            BigInteger publicExponent = keyDetails.getPublicExponent();
            BigInteger privateExponent = keyDetails.getPrivateExponent();
            if (publicExponent != null) {
                RSAKeyParameters publicKeyParameters = new RSAKeyParameters(false, modulus, publicExponent);
                asymmetricKeys.setPublicKey(publicKeyParameters);
            }

            if (privateExponent != null) {
                RSAKeyParameters privateKeyParameters;
                if (keyDetails.isCrt()) {
                    privateKeyParameters = new RSAPrivateCrtKeyParameters(modulus, publicExponent, privateExponent, keyDetails.getPrimeP(), keyDetails.getPrimeQ(), keyDetails.getPrimeExponentP(),
                            keyDetails.getPrimeExponentQ(), keyDetails.getCrtCoefficient());
                } else {
                    privateKeyParameters = new RSAKeyParameters(true, modulus, privateExponent);
                }

                asymmetricKeys.setPrivateKey(privateKeyParameters);
            }

            return asymmetricKeys;

        } catch (Exception e) {
            throw new SmallToolsException("Could not create the keys", e);
        }
    }

    @Override
    protected AsymmetricBlockCipher generateAsymmetricBlockCipher() {
        return new PKCS1Encoding(new RSAEngine());
    }

    @Override
    public AsymmetricKeys generateKeyPair(int keysize) {
        // Generate
        RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
        KeyGenerationParameters param = new RSAKeyGenerationParameters(new BigInteger("65537"), random, keysize, 80);
        rsaKeyPairGenerator.init(param);
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = rsaKeyPairGenerator.generateKeyPair();

        // Store
        AsymmetricKeys asymmetricKeys = new AsymmetricKeys(asymmetricCipherKeyPair.getPublic(), asymmetricCipherKeyPair.getPrivate());
        return asymmetricKeys;
    }

    @Override
    public AsymmetricKeys loadKeysPemFromString(String... pems) {
        RSAKeyDetails keyDetails = new RSAKeyDetails();
        PemReader reader = null;
        try {
            for (String pem : pems) {
                if (pem == null) {
                    continue;
                }
                reader = new PemReader(new StringReader(pem));
                PemObject pemObject;
                while ((pemObject = reader.readPemObject()) != null) {
                    switch (pemObject.getType()) {
                    case "RSA PRIVATE KEY":
                        RSAPrivateKey rsaPrivateKey = RSAPrivateKey.getInstance(pemObject.getContent());
                        keyDetails.setModulus(rsaPrivateKey.getModulus());
                        keyDetails.setPrivateExponent(rsaPrivateKey.getPrivateExponent());
                        keyDetails.setPublicExponent(rsaPrivateKey.getPublicExponent());

                        if (CollectionsTools.isAnyItemNotNull(rsaPrivateKey.getPrime1(), rsaPrivateKey.getPrime2(), rsaPrivateKey.getExponent1(), rsaPrivateKey.getExponent2(),
                                rsaPrivateKey.getCoefficient())) {
                            keyDetails.setCrt(true);
                            keyDetails.setPrimeP(rsaPrivateKey.getPrime1());
                            keyDetails.setPrimeQ(rsaPrivateKey.getPrime2());
                            keyDetails.setPrimeExponentP(rsaPrivateKey.getExponent1());
                            keyDetails.setPrimeExponentQ(rsaPrivateKey.getExponent2());
                            keyDetails.setCrtCoefficient(rsaPrivateKey.getCoefficient());
                        }
                        break;
                    case "PUBLIC KEY":
                        KeyFactory kf = KeyFactory.getInstance("RSA");
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pemObject.getContent());
                        RSAPublicKey rsaPublicKey = (RSAPublicKey) kf.generatePublic(keySpec);
                        keyDetails.setModulus(rsaPublicKey.getModulus());
                        keyDetails.setPublicExponent(rsaPublicKey.getPublicExponent());
                        break;
                    }
                }
            }
            return createKeyPair(keyDetails);
        } catch (Exception e) {
            throw new SmallToolsException("Problem loading the keys", e);
        } finally {
            CloseableTools.close(reader);
        }
    }

    @Override
    public RSAKeyDetails retrieveKeyDetails(AsymmetricKeys keyPair) {

        RSAKeyDetails rsaKeyDetails = new RSAKeyDetails();

        try {
            // Public key
            if (keyPair.getPublicKey() != null) {
                AsymmetricKeyParameter key = keyPair.getPublicKey();

                if (!(key instanceof RSAKeyParameters)) {
                    throw new SmallToolsException("The public key is not of type RSAKeyParameters. Type is " + key.getClass().getName());
                }

                RSAKeyParameters rsaKey = (RSAKeyParameters) key;
                rsaKeyDetails.setModulus(rsaKey.getModulus());
                rsaKeyDetails.setPublicExponent(rsaKey.getExponent());
            }

            // Private key
            if (keyPair.getPrivateKey() != null) {
                AsymmetricKeyParameter key = keyPair.getPrivateKey();
                if (!(key instanceof RSAKeyParameters)) {
                    throw new SmallToolsException("The private key is not of type RSAKeyParameters. Type is " + key.getClass().getName());
                }

                RSAKeyParameters rsaKeyParameters = (RSAKeyParameters) key;
                rsaKeyDetails.setModulus(rsaKeyParameters.getModulus());
                rsaKeyDetails.setPrivateExponent(rsaKeyParameters.getExponent());

                // CRT parameters
                if (key instanceof RSAPrivateCrtKeyParameters) {
                    RSAPrivateCrtKeyParameters rsaPrivateCrtKeyParameters = (RSAPrivateCrtKeyParameters) key;
                    rsaKeyDetails.setCrt(true);
                    rsaKeyDetails.setPrimeP(rsaPrivateCrtKeyParameters.getP());
                    rsaKeyDetails.setPrimeQ(rsaPrivateCrtKeyParameters.getQ());
                    rsaKeyDetails.setPrimeExponentP(rsaPrivateCrtKeyParameters.getDP());
                    rsaKeyDetails.setPrimeExponentQ(rsaPrivateCrtKeyParameters.getDQ());
                    rsaKeyDetails.setCrtCoefficient(rsaPrivateCrtKeyParameters.getQInv());
                }
            }

            return rsaKeyDetails;

        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Could not retrieve the details", e);
        }

    }

    @Override
    public void saveKeysPem(AsymmetricKeys keyPair, String fileName) {
        AssertTools.assertNotNull(keyPair, "The keypair needs to be set to save it");
        AssertTools.assertNotNull(keyPair.getPrivateKey(), "The private key needs to be set to save it");
        AssertTools.assertNotNull(keyPair.getPublicKey(), "The public key needs to be set to save it");
        PemWriter pemWriter = null;
        try {
            pemWriter = new PemWriter(new FileWriter(fileName));
            PemObjectGenerator pemObjectGenerator = new JcaMiscPEMGenerator(retrieveKeyDetails(keyPair).getJcaPrivateKey());
            pemWriter.writeObject(pemObjectGenerator);
            pemObjectGenerator = new JcaMiscPEMGenerator(retrieveKeyDetails(keyPair).getJcaPublicKey());
            pemWriter.writeObject(pemObjectGenerator);
        } catch (Exception e) {
            throw new SmallToolsException("Could not save keys", e);
        } finally {
            CloseableTools.close(pemWriter);
        }
    }

    @Override
    public void savePrivateKeyPem(AsymmetricKeys keyPair, Writer writer) {
        AssertTools.assertNotNull(keyPair, "The keypair needs to be set to save it");
        AssertTools.assertNotNull(keyPair.getPrivateKey(), "The private key needs to be set to save it");
        PemWriter pemWriter = null;
        try {
            pemWriter = new PemWriter(writer);
            PemObjectGenerator pemObjectGenerator = new JcaMiscPEMGenerator(retrieveKeyDetails(keyPair).getJcaPrivateKey());
            pemWriter.writeObject(pemObjectGenerator);
        } catch (Exception e) {
            throw new SmallToolsException("Could not save key", e);
        } finally {
            CloseableTools.close(pemWriter);
        }
    }

    @Override
    public void savePublicKeyPem(AsymmetricKeys keyPair, String fileName) {
        try {
            savePublicKeyPem(keyPair, new FileWriter(fileName));
        } catch (IOException e) {
            throw new SmallToolsException("Could not save key", e);
        }
    }

    @Override
    public void savePublicKeyPem(AsymmetricKeys keyPair, Writer writer) {
        AssertTools.assertNotNull(keyPair, "The public keys need to be set to save it");
        AssertTools.assertNotNull(keyPair.getPublicKey(), "The public key needs to be set to save it");
        PemWriter pemWriter = null;
        try {
            pemWriter = new PemWriter(writer);
            PemObjectGenerator pemObjectGenerator = new JcaMiscPEMGenerator(retrieveKeyDetails(keyPair).getJcaPublicKey());
            pemWriter.writeObject(pemObjectGenerator);
        } catch (Exception e) {
            throw new SmallToolsException("Could not save key", e);
        } finally {
            CloseableTools.close(pemWriter);
        }
    }

}
