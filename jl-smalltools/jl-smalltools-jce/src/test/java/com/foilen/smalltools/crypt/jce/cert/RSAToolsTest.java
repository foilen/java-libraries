package com.foilen.smalltools.crypt.jce.cert;

import com.foilen.smalltools.crypt.jce.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.jce.asymmetric.RSACrypt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAToolsTest {

    private final RSACrypt rsaCrypt = new RSACrypt();

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
