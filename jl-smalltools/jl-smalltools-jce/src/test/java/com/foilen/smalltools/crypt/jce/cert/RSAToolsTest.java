package com.foilen.smalltools.crypt.jce.cert;

import com.foilen.smalltools.crypt.jce.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.jce.asymmetric.RSACrypt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

public class RSAToolsTest {

    private RSACrypt rsaCrypt = new RSACrypt();

    @Test
    public void testLoadPemPkcs7FromString() throws Exception {
        // Generate a root cert and a node cert
        AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate rootCertificate = new RSACertificate(rootKeys);
        rootCertificate.selfSign(new CertificateDetails().setCommonName("pkcs7-root"));

        AsymmetricKeys nodeKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate nodeCertificate = rootCertificate.signPublicKey(nodeKeys, new CertificateDetails().setCommonName("pkcs7-node"));

        // Build a PKCS#7 PEM string containing both certificates
        // PKCS#7 SignedData with only certificates can be represented as a concatenation of DER-encoded certs
        // wrapped in a PKCS7 ContentInfo. For testing purposes, we use CertificateFactory to produce the PKCS#7 bag.
        // A simpler approach: build PKCS#7 manually using DER from the two certificates.
        byte[] cert1Der = rootCertificate.getCertificate().getEncoded();
        byte[] cert2Der = nodeCertificate.getCertificate().getEncoded();
        String pkcs7Pem = buildSimplePkcs7Pem(cert1Der, cert2Der);

        // Load the PKCS#7
        List<RSACertificate> loaded = RSATools.loadPemPkcs7FromString(pkcs7Pem);
        Assertions.assertEquals(2, loaded.size());

        // The certs should be the root and node (order may vary, check thumbprints)
        List<String> thumbprints = loaded.stream()
                .map(RSACertificate::getThumbprint)
                .sorted()
                .toList();
        List<String> expected = Stream.of(rootCertificate.getThumbprint(), nodeCertificate.getThumbprint())
                .sorted()
                .toList();
        Assertions.assertEquals(expected, thumbprints);
    }

    @Test
    public void testLoadPemPkcs7FromFile() throws Exception {
        AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate rootCertificate = new RSACertificate(rootKeys);
        rootCertificate.selfSign(new CertificateDetails().setCommonName("pkcs7-file-root"));

        byte[] cert1Der = rootCertificate.getCertificate().getEncoded();
        String pkcs7Pem = buildSimplePkcs7Pem(cert1Der);

        File tmpFile = File.createTempFile("junit-pkcs7", ".pem");
        tmpFile.deleteOnExit();
        Files.writeString(tmpFile.toPath(), pkcs7Pem);

        List<RSACertificate> loaded = RSATools.loadPemPkcs7FromFile(tmpFile.getAbsolutePath());
        Assertions.assertEquals(1, loaded.size());
        Assertions.assertEquals(rootCertificate.getThumbprint(), loaded.get(0).getThumbprint());
    }

    @Test
    public void testSaveCsrPkcs10Pem() throws Exception {
        AsymmetricKeys keys = rsaCrypt.generateKeyPair(2048);
        RSACertificate certificate = new RSACertificate(keys);
        certificate.selfSign(new CertificateDetails().setCommonName("csr-test"));

        // Save CSR to string and verify PEM format
        String csrPem = RSATools.saveCsrPkcs10PemAsString(certificate);
        Assertions.assertTrue(csrPem.contains("-----BEGIN CERTIFICATE REQUEST-----"));
        Assertions.assertTrue(csrPem.contains("-----END CERTIFICATE REQUEST-----"));

        // Decode and check that the public key in the CSR matches the original
        String base64Part = csrPem
                .replace("-----BEGIN CERTIFICATE REQUEST-----", "")
                .replace("-----END CERTIFICATE REQUEST-----", "")
                .trim();
        byte[] csrDer = Base64.getMimeDecoder().decode(base64Part);
        // The CSR DER structure is: SEQUENCE { CertificationRequestInfo, AlgorithmIdentifier, BIT STRING }
        // CertificationRequestInfo is: SEQUENCE { version, subject, SubjectPublicKeyInfo, attributes }
        // We parse the SubjectPublicKeyInfo out of it and verify it matches the original public key
        PublicKey publicKeyFromCsr = extractPublicKeyFromCsrDer(csrDer);
        Assertions.assertArrayEquals(keys.getPublicKey().getEncoded(), publicKeyFromCsr.getEncoded());
    }

    @Test
    public void testSaveCsrPkcs10PemToFile() throws Exception {
        AsymmetricKeys keys = rsaCrypt.generateKeyPair(2048);
        RSACertificate certificate = new RSACertificate(keys);
        certificate.selfSign(new CertificateDetails().setCommonName("csr-file-test"));

        File tmpFile = File.createTempFile("junit-csr", ".pem");
        tmpFile.deleteOnExit();

        RSATools.saveCsrPkcs10Pem(certificate, tmpFile.getAbsolutePath());

        String content = java.nio.file.Files.readString(tmpFile.toPath());
        Assertions.assertTrue(content.contains("-----BEGIN CERTIFICATE REQUEST-----"));
        Assertions.assertTrue(content.contains("-----END CERTIFICATE REQUEST-----"));
    }

    /**
     * Build a PKCS#7 SignedData PEM containing only certificates (degenerate case).
     * This uses ASN.1 DER encoding manually (no BouncyCastle dependency).
     * Structure (simplified):
     * ContentInfo ::= SEQUENCE {
     * contentType OID (1.2.840.113549.1.7.2 signedData),
     * content [0] EXPLICIT SignedData
     * }
     * SignedData ::= SEQUENCE {
     * version INTEGER (1),
     * digestAlgorithms SET {},
     * encapContentInfo SEQUENCE { OID (1.2.840.113549.1.7.1) },
     * certificates [0] IMPLICIT SEQUENCE OF Certificate,
     * signerInfos SET {}
     * }
     */
    private String buildSimplePkcs7Pem(byte[]... certDers) {
        // Build the certificates [0] IMPLICIT - just concatenate the DER certs
        byte[] certsContent = concatArrays(certDers);
        byte[] certsTagged = tlv(0xA0, certsContent);

        // version INTEGER 1
        byte[] version = tlv(0x02, new byte[]{0x01});
        // digestAlgorithms SET {}
        byte[] digestAlgs = tlv(0x31, new byte[0]);
        // encapContentInfo SEQUENCE { OID id-data }
        byte[] idDataOid = oidEncoded("1.2.840.113549.1.7.1");
        byte[] encapContentInfo = tlv(0x30, idDataOid);
        // signerInfos SET {}
        byte[] signerInfos = tlv(0x31, new byte[0]);

        byte[] signedDataContent = concatArrays(version, digestAlgs, encapContentInfo, certsTagged, signerInfos);
        byte[] signedData = tlv(0x30, signedDataContent);

        // ContentInfo
        byte[] signedDataOid = oidEncoded("1.2.840.113549.1.7.2");
        byte[] contentTagged = tlv(0xA0, signedData);
        byte[] contentInfo = tlv(0x30, concatArrays(signedDataOid, contentTagged));

        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(contentInfo);
        return "-----BEGIN PKCS7-----\n" + base64 + "\n-----END PKCS7-----\n";
    }

    private byte[] oidEncoded(String dotNotation) {
        String[] parts = dotNotation.split("\\.");
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        int first = Integer.parseInt(parts[0]) * 40 + Integer.parseInt(parts[1]);
        buf.write(first);
        for (int i = 2; i < parts.length; i++) {
            long v = Long.parseLong(parts[i]);
            if (v < 128) {
                buf.write((int) v);
            } else {
                byte[] enc = encodeBase128(v);
                for (byte b : enc) buf.write(b);
            }
        }
        return tlv(0x06, buf.toByteArray());
    }

    private byte[] encodeBase128(long value) {
        int numBytes = 0;
        long tmp = value;
        while (tmp > 0) {
            numBytes++;
            tmp >>= 7;
        }
        byte[] result = new byte[numBytes];
        for (int i = numBytes - 1; i >= 0; i--) {
            result[i] = (byte) (value & 0x7F);
            if (i < numBytes - 1) result[i] |= (byte) 0x80;
            value >>= 7;
        }
        return result;
    }

    private byte[] tlv(int tag, byte[] value) {
        byte[] len = derLen(value.length);
        byte[] result = new byte[1 + len.length + value.length];
        result[0] = (byte) tag;
        System.arraycopy(len, 0, result, 1, len.length);
        System.arraycopy(value, 0, result, 1 + len.length, value.length);
        return result;
    }

    private byte[] derLen(int length) {
        if (length < 0x80) return new byte[]{(byte) length};
        else if (length < 0x100) return new byte[]{(byte) 0x81, (byte) length};
        else if (length < 0x10000) return new byte[]{(byte) 0x82, (byte) (length >> 8), (byte) length};
        else return new byte[]{(byte) 0x83, (byte) (length >> 16), (byte) (length >> 8), (byte) length};
    }

    private byte[] concatArrays(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) total += a.length;
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, result, pos, a.length);
            pos += a.length;
        }
        return result;
    }

    /**
     * Extract the SubjectPublicKeyInfo from a CSR DER byte array.
     * CSR structure: SEQUENCE { CertificationRequestInfo, AlgId, BitString }
     * CertificationRequestInfo: SEQUENCE { version, subject, SubjectPublicKeyInfo, attributes }
     */
    private PublicKey extractPublicKeyFromCsrDer(byte[] csrDer) throws Exception {
        // Parse outer SEQUENCE
        int[] outerSeqResult = new int[1];
        byte[] outerContent = parseSequence(csrDer, 0, outerSeqResult);
        // Parse CertificationRequestInfo (first SEQUENCE)
        int[] csrInfoResult = new int[1];
        byte[] csrInfoContent = parseSequence(outerContent, 0, csrInfoResult);
        // Skip version INTEGER
        int pos = 0;
        int[] versionLen = new int[1];
        pos = skipElement(csrInfoContent, pos, versionLen);
        // Skip subject SEQUENCE
        int[] subjectLen = new int[1];
        pos = skipElement(csrInfoContent, pos, subjectLen);
        // Next is SubjectPublicKeyInfo SEQUENCE
        int spkiTag = csrInfoContent[pos] & 0xFF;
        int spkiLenBytes = 0;
        int spkiLen = 0;
        if ((csrInfoContent[pos + 1] & 0xFF) < 0x80) {
            spkiLen = csrInfoContent[pos + 1] & 0xFF;
            spkiLenBytes = 1;
        } else {
            int numBytes = (csrInfoContent[pos + 1] & 0xFF) & 0x7F;
            for (int i = 0; i < numBytes; i++) {
                spkiLen = (spkiLen << 8) | (csrInfoContent[pos + 2 + i] & 0xFF);
            }
            spkiLenBytes = 1 + numBytes;
        }
        int spkiStart = pos;
        int spkiTotal = 1 + spkiLenBytes + spkiLen;
        byte[] spkiDer = new byte[spkiTotal];
        System.arraycopy(csrInfoContent, spkiStart, spkiDer, 0, spkiTotal);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(spkiDer));
    }

    private byte[] parseSequence(byte[] data, int offset, int[] nextOffset) {
        int pos = offset;
        // Expect 0x30 SEQUENCE
        pos++; // skip tag
        int len = 0;
        int lenBytes;
        if ((data[pos] & 0xFF) < 0x80) {
            len = data[pos] & 0xFF;
            lenBytes = 1;
        } else {
            int numBytes = (data[pos] & 0xFF) & 0x7F;
            for (int i = 0; i < numBytes; i++) {
                len = (len << 8) | (data[pos + 1 + i] & 0xFF);
            }
            lenBytes = 1 + numBytes;
        }
        pos += lenBytes;
        byte[] content = new byte[len];
        System.arraycopy(data, pos, content, 0, len);
        nextOffset[0] = pos + len;
        return content;
    }

    private int skipElement(byte[] data, int pos, int[] lenOut) {
        pos++; // skip tag
        int len;
        int lenBytes;
        if ((data[pos] & 0xFF) < 0x80) {
            len = data[pos] & 0xFF;
            lenBytes = 1;
        } else {
            int numBytes = (data[pos] & 0xFF) & 0x7F;
            len = 0;
            for (int i = 0; i < numBytes; i++) {
                len = (len << 8) | (data[pos + 1 + i] & 0xFF);
            }
            lenBytes = 1 + numBytes;
        }
        lenOut[0] = len;
        return pos + lenBytes + len;
    }

}
