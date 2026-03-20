package com.foilen.smalltools.crypt.jce.cert;

import com.foilen.smalltools.crypt.jce.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.jce.cert.trustmanager.RSATrustManagerFactory;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.FileTools;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Base64;

/**
 * Some tools to help converting from the tools in the library to standard Java.
 */
public class RSATools {

    /**
     * Create a {@link KeyManagerFactory} from a {@link RSACertificate}.
     * <p>
     * The key password will be "123".
     *
     * @param rsaCertificate the certificate
     * @return the key manager factory
     */
    public static KeyManagerFactory createKeyManagerFactory(RSACertificate rsaCertificate) {
        char[] keyPassword = new char[]{'1', '2', '3'};
        return createKeyManagerFactory(rsaCertificate, keyPassword);
    }

    /**
     * Create a {@link KeyManagerFactory} from a {@link RSACertificate}.
     *
     * @param rsaCertificate the certificate
     * @param keyPassword    the password for the key
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
     * @param asymmetricKeys the asymmetric keys
     * @return the Java keyPair
     */
    public static KeyPair createKeyPair(AsymmetricKeys asymmetricKeys) {
        PublicKey publicKey = createPublicKey(asymmetricKeys);
        PrivateKey privateKey = createPrivateKey(asymmetricKeys);
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Create a {@link KeyStore} from a {@link RSACertificate}. This is for the private key.
     * <p>
     * The key password will be "123".
     *
     * @param rsaCertificate the certificate
     * @return the keystore
     */
    public static KeyStore createKeyStore(RSACertificate rsaCertificate) {
        char[] keyPassword = new char[]{'1', '2', '3'};
        return createKeyStore(rsaCertificate, keyPassword);
    }

    /**
     * Create a {@link KeyStore} from a {@link RSACertificate}. This is for the private key.
     *
     * @param rsaCertificate the certificate
     * @param keyPassword    the password for the key
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
            keyStore.setKeyEntry(alias, key, keyPassword, new Certificate[]{certificate});
            return keyStore;
        } catch (Exception e) {
            throw new SmallToolsException("Problem creating the keystore", e);
        }
    }

    /**
     * Create a {@link KeyStore} from a {@link RSATrustedCertificates}. It is taking only the trusted certificates ; not the intermediates ones.
     * <p>
     * The aliases will be the certificate's thumbprint to make sure they are unique.
     *
     * @param rsaTrustedCertificates the certificates that are trusted
     * @return the keystore
     */
    static public KeyStore createKeyStore(RSATrustedCertificates rsaTrustedCertificates) {
        try {
            // Create empty keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);

            // Fill it with the trusted certificates
            for (var rsaCertificate : rsaTrustedCertificates.getTrustedCertificates()) {
                String alias = rsaCertificate.getThumbprint();
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
     * @param asymmetricKeys the asymmetric keys
     * @return the Java key
     * @deprecated use "asymmetricKeys.getPrivateKey()" directly
     */
    @Deprecated
    public static PrivateKey createPrivateKey(AsymmetricKeys asymmetricKeys) {
        return asymmetricKeys.getPrivateKey();
    }

    /**
     * Create a {@link Key} from the public {@link AsymmetricKeys}.
     *
     * @param asymmetricKeys the asymmetric keys
     * @return the Java key
     * @deprecated use "asymmetricKeys.getPublicKey()" directly
     */
    @Deprecated
    public static PublicKey createPublicKey(AsymmetricKeys asymmetricKeys) {
        return asymmetricKeys.getPublicKey();
    }

    /**
     * Create a {@link TrustManagerFactory} from a {@link KeyStore}.
     *
     * @param keyStore the keyStore
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
     * Create a {@link TrustManagerFactory} from a {@link RSATrustedCertificates}.
     *
     * @param rsaTrustedCertificates the certificates that are trusted
     * @return the trust manager factory
     */
    static public RSATrustManagerFactory createTrustManagerFactory(RSATrustedCertificates rsaTrustedCertificates) {
        RSATrustManagerFactory trustManagerFactory = RSATrustManagerFactory.getInstance();
        trustManagerFactory.init(rsaTrustedCertificates);
        return trustManagerFactory;
    }

    /**
     * Save a PKCS#10 Certificate Signing Request (CSR) as a PEM file for the given certificate's public key.
     * <p>
     * The {@link RSACertificate} must have keys set (both public and private) to generate and sign the CSR.
     *
     * @param certificate the certificate whose public key will be included in the CSR
     * @param fileName    the full path to the output PEM file
     */
    public static void saveCsrPkcs10Pem(RSACertificate certificate, String fileName) {
        try {
            String pem = saveCsrPkcs10PemAsString(certificate);
            FileTools.writeFile(pem, fileName);
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Problem saving PKCS#10 CSR", e);
        }
    }

    /**
     * Save a PKCS#10 Certificate Signing Request (CSR) as a PEM string for the given certificate's public key.
     * <p>
     * The {@link RSACertificate} must have keys set (both public and private) to generate and sign the CSR.
     *
     * @param certificate the certificate whose public key will be included in the CSR
     * @return the PEM string
     */
    public static String saveCsrPkcs10PemAsString(RSACertificate certificate) {
        try {
            AsymmetricKeys keys = certificate.getKeysForSigning();
            if (keys == null || keys.getPrivateKey() == null) {
                throw new SmallToolsException("The certificate must have a private key to generate a CSR");
            }

            PublicKey publicKey = keys.getPublicKey();
            PrivateKey privateKey = keys.getPrivateKey();

            // Use the full subject DN from the certificate
            byte[] subject = certificate.getCertificate().getSubjectX500Principal().getEncoded();

            // SubjectPublicKeyInfo: already DER-encoded from the public key
            byte[] spki = publicKey.getEncoded();

            // attributes [0] IMPLICIT Attributes (empty set)
            byte[] attributes = new byte[]{(byte) 0xA0, 0x00};

            // CertificationRequestInfo ::= SEQUENCE { version INTEGER, subject Name, subjectPublicKeyInfo, attributes }
            byte[] version = derInteger(java.math.BigInteger.ZERO);
            byte[] csrInfo = derSequence(concat(version, subject, spki, attributes));

            // Sign the CertificationRequestInfo
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(csrInfo);
            byte[] signatureValue = signer.sign();

            // AlgorithmIdentifier: SEQUENCE { OID sha256WithRSAEncryption, NULL }
            byte[] algId = derSequence(concat(derOid("1.2.840.113549.1.1.11"), new byte[]{0x05, 0x00}));

            // BIT STRING for signature
            byte[] sigBitString = derBitString(signatureValue);

            // CertificationRequest ::= SEQUENCE { info, algorithm, signature }
            byte[] csr = derSequence(concat(csrInfo, algId, sigBitString));

            // Encode as PEM
            String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(csr);
            return "-----BEGIN CERTIFICATE REQUEST-----\n" + base64 + "\n-----END CERTIFICATE REQUEST-----\n";
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Problem generating PKCS#10 CSR", e);
        }
    }

    // ------- DER encoding helpers (used for CSR generation) -------

    private static byte[] derSequence(byte[] content) {
        return derTlv(0x30, content);
    }

    private static byte[] derSet(byte[] content) {
        return derTlv(0x31, content);
    }

    private static byte[] derInteger(java.math.BigInteger value) {
        return derTlv(0x02, value.toByteArray());
    }

    private static byte[] derOid(String dotNotation) {
        String[] parts = dotNotation.split("\\.");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int first = Integer.parseInt(parts[0]) * 40 + Integer.parseInt(parts[1]);
        buf.write(first);
        for (int i = 2; i < parts.length; i++) {
            long component = Long.parseLong(parts[i]);
            if (component < 128) {
                buf.write((int) component);
            } else {
                byte[] encoded = encodeBase128(component);
                for (byte b : encoded) {
                    buf.write(b);
                }
            }
        }
        return derTlv(0x06, buf.toByteArray());
    }

    private static byte[] encodeBase128(long value) {
        if (value == 0) {
            return new byte[]{0};
        }
        int numBytes = 0;
        long tmp = value;
        while (tmp > 0) {
            numBytes++;
            tmp >>= 7;
        }
        byte[] result = new byte[numBytes];
        for (int i = numBytes - 1; i >= 0; i--) {
            result[i] = (byte) (value & 0x7F);
            if (i < numBytes - 1) {
                result[i] |= (byte) 0x80;
            }
            value >>= 7;
        }
        return result;
    }

    private static byte[] derUtf8String(String value) {
        try {
            return derTlv(0x0C, value.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new SmallToolsException("UTF-8 not supported", e);
        }
    }

    private static byte[] derBitString(byte[] value) {
        byte[] content = new byte[value.length + 1];
        content[0] = 0x00;
        System.arraycopy(value, 0, content, 1, value.length);
        return derTlv(0x03, content);
    }

    private static byte[] derTlv(int tag, byte[] value) {
        byte[] lengthBytes = derLength(value.length);
        byte[] result = new byte[1 + lengthBytes.length + value.length];
        result[0] = (byte) tag;
        System.arraycopy(lengthBytes, 0, result, 1, lengthBytes.length);
        System.arraycopy(value, 0, result, 1 + lengthBytes.length, value.length);
        return result;
    }

    private static byte[] derLength(int length) {
        if (length < 0x80) {
            return new byte[]{(byte) length};
        } else if (length < 0x100) {
            return new byte[]{(byte) 0x81, (byte) length};
        } else if (length < 0x10000) {
            return new byte[]{(byte) 0x82, (byte) (length >> 8), (byte) length};
        } else {
            return new byte[]{(byte) 0x83, (byte) (length >> 16), (byte) (length >> 8), (byte) length};
        }
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) {
            total += a.length;
        }
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    private RSATools() {
    }

}
