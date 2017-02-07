/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.cert;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.params.RSAPrivateCrtKeyParameters;

import com.foilen.smalltools.crypt.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some tools to help converting from the tools in the library to standard Java.
 *
 * <pre>
 * Dependencies:
 * compile 'com.madgag.spongycastle:prov:1.51.0.0'
 * compile 'com.madgag.spongycastle:pkix:1.51.0.0'
 * compile 'com.madgag.spongycastle:pg:1.51.0.0'
 * </pre>
 */
public class RSATools {

    /**
     * Create a {@link KeyManagerFactory} from a {@link RSACertificate}.
     *
     * The key password will be "123".
     *
     * @param rsaCertificate
     *            the certificate
     * @return the key manager factory
     */
    public static KeyManagerFactory createKeyManagerFactory(RSACertificate rsaCertificate) {
        char[] keyPassword = new char[] { '1', '2', '3' };
        return createKeyManagerFactory(rsaCertificate, keyPassword);
    }

    /**
     * Create a {@link KeyManagerFactory} from a {@link RSACertificate}.
     *
     * @param rsaCertificate
     *            the certificate
     * @param keyPassword
     *            the password for the key
     * @return the key manager factory
     */
    public static KeyManagerFactory createKeyManagerFactory(RSACertificate rsaCertificate, char[] keyPassword) {
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(createKeyStore(rsaCertificate), keyPassword);
            return keyManagerFactory;
        } catch (Exception e) {
            throw new SmallToolsException("Problem creating the key manager factory", e);
        }
    }

    /**
     * Create a {@link KeyPair} from the {@link AsymmetricKeys}.
     *
     * @param asymmetricKeys
     *            the asymmetric keys
     * @return the Java keyPair
     */
    public static KeyPair createKeyPair(AsymmetricKeys asymmetricKeys) {
        PublicKey publicKey = createPublicKey(asymmetricKeys);
        PrivateKey privateKey = createPrivateKey(asymmetricKeys);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Create a {@link KeyStore} from a {@link RSACertificate}. This is for the private key.
     *
     * The key password will be "123".
     *
     * @param rsaCertificate
     *            the certificate
     * @return the keystore
     */
    public static KeyStore createKeyStore(RSACertificate rsaCertificate) {
        char[] keyPassword = new char[] { '1', '2', '3' };
        return createKeyStore(rsaCertificate, keyPassword);
    }

    /**
     * Create a {@link KeyStore} from a {@link RSACertificate}. This is for the private key.
     *
     * @param rsaCertificate
     *            the certificate
     * @param keyPassword
     *            the password for the key
     * @return the keystore
     */
    public static KeyStore createKeyStore(RSACertificate rsaCertificate, char[] keyPassword) {
        try {
            // Create empty keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);

            // Fill it with the certificates
            String alias = rsaCertificate.getCommonName();
            Certificate certificate = rsaCertificate.getCertificate();
            keyStore.setCertificateEntry(alias, certificate);
            Key key = createPrivateKey(rsaCertificate.getKeysForSigning());
            keyStore.setKeyEntry(alias, key, keyPassword, new Certificate[] { certificate });
            return keyStore;
        } catch (Exception e) {
            throw new SmallToolsException("Problem creating the keystore", e);
        }
    }

    /**
     * Create a {@link KeyStore} from a {@link RSATrustedCertificates}. It is taking only the trusted certificates ; not the intermediates ones.
     *
     * @param rsaTrustedCertificates
     *            the certificates that are trusted
     * @return the keystore
     */
    static public KeyStore createKeyStore(RSATrustedCertificates rsaTrustedCertificates) {
        try {
            // Create empty keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);

            // Fill it with the trusted certificates
            for (RSACertificate rsaCertificate : rsaTrustedCertificates.getTrustedCertificates()) {
                String alias = rsaCertificate.getCommonName();
                Certificate certificate = rsaCertificate.getCertificate();
                keyStore.setCertificateEntry(alias, certificate);
            }
            return keyStore;
        } catch (Exception e) {
            throw new SmallToolsException("Problem creating the keystore", e);
        }
    }

    /**
     * Create a {@link Key} from the private {@link AsymmetricKeys}.
     *
     * @param asymmetricKeys
     *            the asymmetric keys
     * @return the Java key
     */
    public static PrivateKey createPrivateKey(AsymmetricKeys asymmetricKeys) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateCrtKeyParameters privateKeyParameters = (RSAPrivateCrtKeyParameters) asymmetricKeys.getPrivateKey();
            RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(privateKeyParameters.getModulus(), privateKeyParameters.getPublicExponent(), privateKeyParameters.getExponent(),
                    privateKeyParameters.getP(), privateKeyParameters.getQ(), privateKeyParameters.getDP(), privateKeyParameters.getDQ(), privateKeyParameters.getQInv());
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new SmallToolsException("Problem generating the key", e);
        }
    }

    /**
     * Create a {@link Key} from the public {@link AsymmetricKeys}.
     *
     * @param asymmetricKeys
     *            the asymmetric keys
     * @return the Java key
     */
    public static PublicKey createPublicKey(AsymmetricKeys asymmetricKeys) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAKeyParameters publicKeyParameters = (RSAKeyParameters) asymmetricKeys.getPublicKey();
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(publicKeyParameters.getModulus(), publicKeyParameters.getExponent());
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new SmallToolsException("Problem generating the key", e);
        }
    }

    /**
     * Create a {@link TrustManagerFactory} from a {@link KeyStore}.
     *
     * @param keyStore
     *            the keyStore
     * @return the trust manager factory
     */
    static public TrustManagerFactory createTrustManagerFactory(KeyStore keyStore) {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            return trustManagerFactory;
        } catch (Exception e) {
            throw new SmallToolsException("Problem creating the trust manager factory", e);
        }
    }

    /**
     * Create a {@link TrustManagerFactory} from a {@link RSATrustedCertificates}. It is taking only the trusted certificates ; not the intermediates ones.
     *
     * @param rsaTrustedCertificates
     *            the certificates that are trusted
     * @return the trust manager factory
     */
    static public TrustManagerFactory createTrustManagerFactory(RSATrustedCertificates rsaTrustedCertificates) {
        return createTrustManagerFactory(createKeyStore(rsaTrustedCertificates));
    }

    private RSATools() {
    }

}
