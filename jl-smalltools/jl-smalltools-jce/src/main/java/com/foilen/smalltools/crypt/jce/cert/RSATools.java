package com.foilen.smalltools.crypt.jce.cert;

import com.foilen.smalltools.crypt.jce.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.jce.cert.trustmanager.RSATrustManagerFactory;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.FileTools;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

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
            for (RSACertificate rsaCertificate : rsaTrustedCertificates.getTrustedCertificates()) {
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
     * Load a list of certificates from a PKCS#7 PEM file
     *
     * @param fileName the full path of the PKCS#7 PEM file
     * @return the list of certificates contained in the PKCS#7 bag
     */
    public static List<RSACertificate> loadPemPkcs7FromFile(String fileName) {
        String pem = FileTools.getFileAsString(fileName);
        return loadPemPkcs7FromString(pem);
    }

    /**
     * Load a list of certificates from a PKCS#7 PEM string
     *
     * @param pem the PEM string
     * @return the list of certificates contained in the PKCS#7 bag
     */
    public static List<RSACertificate> loadPemPkcs7FromString(String pem) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<RSACertificate> result = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new StringReader(pem));
            StringBuilder sb = new StringBuilder();
            String line;
            String blockType = null; // "PKCS7" or "CERTIFICATE"
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("-----BEGIN PKCS7-----")) {
                    blockType = "PKCS7";
                    sb.setLength(0);
                } else if (line.equals("-----BEGIN CERTIFICATE-----")) {
                    blockType = "CERTIFICATE";
                    sb.setLength(0);
                } else if (line.equals("-----END PKCS7-----") && "PKCS7".equals(blockType)) {
                    byte[] der = Base64.getMimeDecoder().decode(sb.toString());
                    Collection<? extends Certificate> certs = cf.generateCertificates(new ByteArrayInputStream(der));
                    for (Certificate cert : certs) {
                        result.add(new RSACertificate((X509Certificate) cert));
                    }
                    blockType = null;
                    sb.setLength(0);
                } else if (line.equals("-----END CERTIFICATE-----") && "CERTIFICATE".equals(blockType)) {
                    byte[] der = Base64.getMimeDecoder().decode(sb.toString());
                    Certificate cert = cf.generateCertificate(new ByteArrayInputStream(der));
                    result.add(new RSACertificate((X509Certificate) cert));
                    blockType = null;
                    sb.setLength(0);
                } else if (blockType != null) {
                    sb.append(line).append('\n');
                }
            }

            if (result.isEmpty()) {
                throw new SmallToolsException("No PKCS7 or CERTIFICATE block found in PEM");
            }
            return result;
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Problem loading PKCS#7 certificates", e);
        }
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
            String commonName = certificate.getCommonName();

            // Build CertificationRequestInfo (PKCS#10)
            // Subject: SEQUENCE { SET { SEQUENCE { OID, UTF8String } } }
            byte[] cnOid = derOid("2.5.4.3");
            byte[] cnValue = derUtf8String(commonName);
            byte[] atv = derSequence(concat(cnOid, cnValue));
            byte[] rdn = derSet(atv);
            byte[] subject = derSequence(rdn);

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
