package com.foilen.smalltools.crypt.jce.cert;

import com.foilen.smalltools.crypt.jce.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.jce.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.jce.asymmetric.RSAKeyDetails;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.hash.HashSha1;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.FileTools;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * To create self-signed certificates and to sign other certificates.
 *
 * <pre>
 * Usage:
 *
 * // Root
 * AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
 * RSACertificate rootCertificate = new RSACertificate(rootKeys);
 * rootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));
 *
 * // Node
 * AsymmetricKeys nodeKeys = rsaCrypt.generateKeyPair(2048);
 * RSACertificate nodeCertificate = rootCertificate.signPublicKey(nodeKeys, new CertificateDetails().setCommonName("p001.node.foilen.org"));
 *
 * // Fake Root
 * AsymmetricKeys fakeRootKeys = rsaCrypt.generateKeyPair(2048);
 * RSACertificate fakeRootCertificate = new RSACertificate(fakeRootKeys);
 * fakeRootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));
 *
 * // Assert certificates
 * Assert.assertTrue(rootCertificate.isValidSignature(rootCertificate));
 * Assert.assertTrue(nodeCertificate.isValidSignature(rootCertificate));
 * Assert.assertTrue(fakeRootCertificate.isValidSignature(fakeRootCertificate));
 *
 * Assert.assertFalse(rootCertificate.isValidSignature(nodeCertificate));
 * Assert.assertFalse(rootCertificate.isValidSignature(fakeRootCertificate));
 * Assert.assertFalse(nodeCertificate.isValidSignature(nodeCertificate));
 * Assert.assertFalse(nodeCertificate.isValidSignature(fakeRootCertificate));
 * Assert.assertFalse(fakeRootCertificate.isValidSignature(rootCertificate));
 * Assert.assertFalse(fakeRootCertificate.isValidSignature(nodeCertificate));
 * </pre>
 */
public class RSACertificate {

    // OID for Common Name
    private static final String OID_COMMON_NAME = "2.5.4.3";
    // OID for Country
    private static final String OID_COUNTRY = "2.5.4.6";
    // OID for Organization
    private static final String OID_ORGANIZATION = "2.5.4.10";
    // OID for Organizational Unit
    private static final String OID_ORG_UNIT = "2.5.4.11";
    // OID for State/Province
    private static final String OID_STATE = "2.5.4.8";
    // OID for Locality
    private static final String OID_LOCALITY = "2.5.4.7";
    // OID for Subject Alternative Name extension
    private static final String OID_SAN = "2.5.29.17";
    // OID for SHA256withRSA signature algorithm
    private static final String OID_SHA256_WITH_RSA = "1.2.840.113549.1.1.11";
    // OID for rsaEncryption (used in AlgorithmIdentifier for public key)
    private static final String OID_RSA_ENCRYPTION = "1.2.840.113549.1.1.1";

    private static final RSACrypt rsaCrypt = new RSACrypt();

    /**
     * Load the certificate and keys (if present in the file).
     *
     * @param fileName the full path of the file
     * @return the certificate
     */
    public static RSACertificate loadPemFromFile(String fileName) {
        String pem = FileTools.getFileAsString(fileName);
        return loadPemFromString(pem);
    }

    /**
     * Load the certificate and keys (if present in the strings).
     *
     * @param pems the pems (some can be null)
     * @return the certificate
     */
    public static RSACertificate loadPemFromString(String... pems) {
        RSACertificate certificate = new RSACertificate();
        try {
            // Keys if present
            certificate.keysForSigning = rsaCrypt.loadKeysPemFromString(pems);

            // Certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (String pem : pems) {
                if (pem == null) {
                    continue;
                }
                // Extract CERTIFICATE blocks
                BufferedReader reader = new BufferedReader(new StringReader(pem));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("-----BEGIN CERTIFICATE-----")) {
                        StringBuilder sb = new StringBuilder();
                        String certLine;
                        while ((certLine = reader.readLine()) != null) {
                            certLine = certLine.trim();
                            if (certLine.equals("-----END CERTIFICATE-----")) {
                                break;
                            }
                            sb.append(certLine).append('\n');
                        }
                        byte[] der = Base64.getMimeDecoder().decode(sb.toString());
                        certificate.certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));
                    }
                }
            }

            return certificate;
        } catch (Exception e) {
            throw new SmallToolsException("Problem loading the certificate", e);
        }
    }

    private X509Certificate certificate;

    private AsymmetricKeys keysForSigning;

    /**
     * Create an empty holder.
     */
    public RSACertificate() {
    }

    /**
     * Create a holder with the keys to sign.
     *
     * @param keysForSigning the keys to sign
     */
    public RSACertificate(AsymmetricKeys keysForSigning) {
        this.keysForSigning = keysForSigning;
    }

    /**
     * Create a holder with the certificate.
     *
     * @param certificate the certificate
     */
    public RSACertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    /**
     * Get the Java certificate.
     *
     * @return the Java certificate
     */
    public X509Certificate getCertificate() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        return certificate;
    }

    /**
     * Get the first certificate's common name.
     *
     * @return the common name
     */
    public String getCommonName() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        // Extract from Subject DN
        String dn = certificate.getSubjectX500Principal().getName();
        return extractCN(dn);
    }

    /**
     * Get the certificate's common names.
     *
     * @return the common names
     */
    public Set<String> getCommonNames() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        Set<String> commonNames = new HashSet<>();
        String dn = certificate.getSubjectX500Principal().getName();
        // DN is in RFC2253 format: CN=value,O=...
        for (String part : splitDn(dn)) {
            part = part.trim();
            if (part.startsWith("CN=")) {
                commonNames.add(unescapeDnValue(part.substring(3)));
            }
        }
        return commonNames;
    }

    /**
     * Get the ending date of this certificate.
     *
     * @return the ending date
     */
    public Date getEndDate() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        return certificate.getNotAfter();
    }

    /**
     * Get the keys to sign.
     *
     * @return the keys to sign
     */
    public AsymmetricKeys getKeysForSigning() {
        // Fill the public key if missing
        if (certificate != null) {

            // Create missing keys
            if (keysForSigning == null) {
                keysForSigning = new AsymmetricKeys();
            }

            // Create missing public key
            if (keysForSigning.getPublicKey() == null) {
                keysForSigning.setPublicKey(certificate.getPublicKey());
            }
        }
        return keysForSigning;
    }

    /**
     * Get the certificate's subject DN in RFC 2253 format.
     *
     * @return the subject DN string
     */
    public String getSubjectDn() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        return certificate.getSubjectX500Principal().getName();
    }

    /**
     * Get the certificate's issuer DN in RFC 2253 format.
     *
     * @return the issuer DN string
     */
    public String getIssuerDn() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        return certificate.getIssuerX500Principal().getName();
    }

    /**
     * Get the starting date of this certificate.
     *
     * @return the starting date
     */
    public Date getStartDate() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        return certificate.getNotBefore();
    }

    /**
     * Get all the Subject Alternative Names.
     *
     * @return the SANs
     */
    public Set<String> getSubjectAltNames() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");

        Set<String> results = new HashSet<>();

        try {
            Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
            if (subjectAlternativeNames == null) {
                return results;
            }

            for (List<?> next : subjectAlternativeNames) {
                results.add(next.get(1).toString());
            }

        } catch (Exception e) {
            throw new SmallToolsException("Problem parsing the certificate", e);
        }
        return results;
    }

    /**
     * Compute the SHA1 thumbprint.
     *
     * @return the SHA1 thumbprint
     */
    public String getThumbprint() {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        try {
            return HashSha1.hashBytes(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new SmallToolsException("Problem getting the thumbprint", e);
        }
    }

    /**
     * Check if the current time is in the certificate dates range.
     *
     * @return true if valid
     */
    public boolean isValidDate() {
        return isValidDate(new Date());
    }

    /**
     * Check if the specified time is in the certificate dates range.
     *
     * @param date the time to check
     * @return true if valid
     */
    public boolean isValidDate(Date date) {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        return DateTools.isAfter(date, certificate.getNotBefore()) && DateTools.isBefore(date, certificate.getNotAfter());
    }

    /**
     * Check if the certificate was signed by the specified public key.
     *
     * @param signerPublicKey the signer's public key
     * @return true if signed by it
     */
    public boolean isValidSignature(PublicKey signerPublicKey) {
        try {
            certificate.verify(signerPublicKey);
            return true;
        } catch (SignatureException | InvalidKeyException e) {
            return false;
        } catch (Exception e) {
            throw new SmallToolsException("Problem validating the certificate", e);
        }
    }

    /**
     * Check if the certificate was signed by the specified public key.
     *
     * @param signerPublicKey the signer's pair of keys that contains the public key
     * @return true if signed by it
     */
    public boolean isValidSignature(AsymmetricKeys signerPublicKey) {
        return isValidSignature(signerPublicKey.getPublicKey());
    }

    /**
     * Check if the certificate was signed by the specified certificate.
     *
     * @param signerCertificate the signer's certificate
     * @return true if signed by it
     */
    public boolean isValidSignature(RSACertificate signerCertificate) {
        return isValidSignature(signerCertificate.certificate.getPublicKey());
    }

    /**
     * Save the certificate in a PEM file.
     *
     * @param fileName the full path to the file
     */
    public void saveCertificatePem(String fileName) {
        try {
            saveCertificatePem(new FileWriter(fileName));
        } catch (IOException e) {
            throw new SmallToolsException("Could not save cert", e);
        }
    }

    /**
     * Save the certificate in a PEM writer.
     *
     * @param writer the writer. Will be closed at the end
     */
    public void saveCertificatePem(Writer writer) {
        AssertTools.assertNotNull(certificate, "The certificate is not set");
        try {
            byte[] encoded = certificate.getEncoded();
            writer.write("-----BEGIN CERTIFICATE-----\n");
            String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
            writer.write(base64);
            writer.write("\n-----END CERTIFICATE-----\n");
            writer.flush();
        } catch (Exception e) {
            throw new SmallToolsException("Could not save cert", e);
        } finally {
            CloseableTools.close(writer);
        }
    }

    /**
     * Save the certificate in a PEM String.
     *
     * @return the pem
     */
    public String saveCertificatePemAsString() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        saveCertificatePem(new OutputStreamWriter(result));
        return result.toString();
    }

    /**
     * Sign the {@link #setKeysForSigning(AsymmetricKeys)} with itself and put it in certificate.
     *
     * @param certificateDetails some information to store in the certificate
     * @return this
     */
    public RSACertificate selfSign(CertificateDetails certificateDetails) {

        AssertTools.assertNotNull(keysForSigning, "The keysForSigning is not set");
        AssertTools.assertNull(certificate, "The certificate already exists");

        try {
            RSAKeyDetails keyDetails = rsaCrypt.retrieveKeyDetails(keysForSigning);
            PrivateKey privKey = keyDetails.getJcaPrivateKey();
            PublicKey publicKey = keyDetails.getJcaPublicKey();

            String cn = certificateDetails.getCommonName();
            certificate = buildCertificate(
                    cn, cn,
                    certificateDetails.getStartDate(),
                    certificateDetails.getEndDate(),
                    certificateDetails.getSerial(),
                    publicKey,
                    privKey,
                    certificateDetails.getSanDns()
            );

            return this;
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Problem signing the key", e);
        }
    }

    /**
     * Set the keys to use for signing.
     *
     * @param keysForSigning the keys to use for signing
     * @return this
     */
    public RSACertificate setKeysForSigning(AsymmetricKeys keysForSigning) {
        this.keysForSigning = keysForSigning;
        return this;
    }

    /**
     * Sign another public key.
     *
     * @param publicKeyToSign    the public key to sign
     * @param certificateDetails some information to store in the certificate
     * @return the new certificate
     */
    public RSACertificate signPublicKey(AsymmetricKeys publicKeyToSign, CertificateDetails certificateDetails) {

        try {
            PrivateKey privKey = rsaCrypt.retrieveKeyDetails(keysForSigning).getJcaPrivateKey();
            PublicKey publicKey = rsaCrypt.retrieveKeyDetails(publicKeyToSign).getJcaPublicKey();

            String issuerCn = getCommonName();
            String subjectCn = certificateDetails.getCommonName();

            X509Certificate newCert = buildCertificate(
                    issuerCn, subjectCn,
                    certificateDetails.getStartDate(),
                    certificateDetails.getEndDate(),
                    certificateDetails.getSerial(),
                    publicKey,
                    privKey,
                    certificateDetails.getSanDns()
            );

            RSACertificate result = new RSACertificate(newCert);
            result.keysForSigning = publicKeyToSign;
            return result;
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Problem signing the key", e);
        }
    }

    // -----------------------------------------------------------------------
    // X.509 certificate builder (pure JCE)
    // -----------------------------------------------------------------------

    /**
     * Build an X.509 v3 certificate using manual DER encoding.
     */
    private X509Certificate buildCertificate(
            String issuerCN, String subjectCN,
            Date notBefore, Date notAfter,
            BigInteger serial,
            PublicKey subjectPublicKey,
            PrivateKey signingKey,
            List<String> sanDns) throws Exception {

        // TBSCertificate fields
        byte[] versionField = derTagged(0xA0, derInteger(BigInteger.TWO)); // version [0] EXPLICIT INTEGER 2
        byte[] serialNumber = derInteger(serial);
        byte[] signatureAlg = derAlgorithmIdentifierSHA256withRSA();
        byte[] issuer = derName(issuerCN);
        byte[] validity = derValidity(notBefore, notAfter);
        byte[] subject = derName(subjectCN);
        byte[] spki = subjectPublicKey.getEncoded(); // already DER encoded SubjectPublicKeyInfo

        byte[] tbsContent;
        if (sanDns != null && !sanDns.isEmpty()) {
            byte[] extensions = derExtensions(sanDns);
            byte[] extensionsTagged = derTagged(0xA3, extensions);
            tbsContent = concat(versionField, serialNumber, signatureAlg, issuer, validity, subject, spki, extensionsTagged);
        } else {
            tbsContent = concat(versionField, serialNumber, signatureAlg, issuer, validity, subject, spki);
        }

        byte[] tbsCertificate = derSequence(tbsContent);

        // Sign the TBS certificate
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(signingKey);
        signer.update(tbsCertificate);
        byte[] signatureValue = signer.sign();

        // Build the full Certificate SEQUENCE
        byte[] signatureAlg2 = derAlgorithmIdentifierSHA256withRSA();
        byte[] signatureBitString = derBitString(signatureValue);
        byte[] certContent = concat(tbsCertificate, signatureAlg2, signatureBitString);
        byte[] certDer = derSequence(certContent);

        // Parse it back as an X509Certificate
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certDer));
    }

    // ------- DER encoding helpers -------

    private byte[] derSequence(byte[] content) {
        return derTlv(0x30, content);
    }

    private byte[] derSet(byte[] content) {
        return derTlv(0x31, content);
    }

    private byte[] derTagged(int tag, byte[] content) {
        return derTlv(tag, content);
    }

    private byte[] derInteger(BigInteger value) {
        return derTlv(0x02, value.toByteArray());
    }

    private byte[] derOid(String dotNotation) {
        String[] parts = dotNotation.split("\\.");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        // First two components encoded together
        int first = Integer.parseInt(parts[0]) * 40 + Integer.parseInt(parts[1]);
        buf.write(first);
        for (int i = 2; i < parts.length; i++) {
            long component = Long.parseLong(parts[i]);
            if (component < 128) {
                buf.write((int) component);
            } else {
                // Multi-byte base-128 encoding
                byte[] encoded = encodeBase128(component);
                for (byte b : encoded) {
                    buf.write(b);
                }
            }
        }
        return derTlv(0x06, buf.toByteArray());
    }

    private byte[] encodeBase128(long value) {
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
                result[i] |= 0x80;
            }
            value >>= 7;
        }
        return result;
    }

    private byte[] derNull() {
        return new byte[]{0x05, 0x00};
    }

    private byte[] derUtf8String(String value) {
        try {
            return derTlv(0x0C, value.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new SmallToolsException("UTF-8 not supported", e);
        }
    }

    private byte[] derIA5String(String value) {
        try {
            return derTlv(0x16, value.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new SmallToolsException("ASCII not supported", e);
        }
    }

    private byte[] derBitString(byte[] value) {
        // Prepend a 0x00 byte (number of unused bits in last byte)
        byte[] content = new byte[value.length + 1];
        content[0] = 0x00;
        System.arraycopy(value, 0, content, 1, value.length);
        return derTlv(0x03, content);
    }

    private byte[] derOctetString(byte[] value) {
        return derTlv(0x04, value);
    }

    private byte[] derBoolean(boolean value) {
        return derTlv(0x01, new byte[]{value ? (byte) 0xFF : (byte) 0x00});
    }

    /**
     * Encode a UTCTime or GeneralizedTime for X.509 validity.
     * Uses UTCTime (YYMMDDHHMMSSZ) for years 1950-2049, GeneralizedTime otherwise.
     */
    private byte[] derTime(Date date) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        if (year >= 1950 && year <= 2049) {
            // UTCTime: YYMMDDHHMMSSZ
            String s = String.format("%02d%02d%02d%02d%02d%02dZ",
                    year % 100,
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND));
            try {
                return derTlv(0x17, s.getBytes("US-ASCII"));
            } catch (UnsupportedEncodingException e) {
                throw new SmallToolsException("ASCII not supported", e);
            }
        } else {
            // GeneralizedTime: YYYYMMDDHHMMSSZ
            String s = String.format("%04d%02d%02d%02d%02d%02dZ",
                    year,
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND));
            try {
                return derTlv(0x18, s.getBytes("US-ASCII"));
            } catch (UnsupportedEncodingException e) {
                throw new SmallToolsException("ASCII not supported", e);
            }
        }
    }

    /**
     * Encode a DN. The subjectString may be a simple CN value like "myhost",
     * or a composite subject string like "myhost,C=CA,O=foilen" where the
     * first segment without an '=' is the CN value and subsequent segments
     * are KEY=VALUE pairs for additional attributes.
     */
    private byte[] derName(String subjectString) {
        // Parse the composite subject string into ordered RDN components
        List<String[]> rdns = parseSubjectString(subjectString);

        // Encode each RDN and concatenate
        ByteArrayOutputStream nameContent = new ByteArrayOutputStream();
        for (String[] rdn : rdns) {
            String key = rdn[0];
            String value = rdn[1];
            String oid = attributeKeyToOid(key);
            byte[] attrOid = derOid(oid);
            byte[] attrValue = "C".equalsIgnoreCase(key) ? derPrintableString(value) : derUtf8String(value);
            byte[] atv = derSequence(concat(attrOid, attrValue));
            byte[] rdnBytes = derSet(atv);
            try {
                nameContent.write(rdnBytes);
            } catch (IOException e) {
                throw new SmallToolsException("Error encoding DN", e);
            }
        }
        return derSequence(nameContent.toByteArray());
    }

    /**
     * Parse a subject string into an ordered list of [key, value] pairs.
     * Input like "test,C=CA,O=foilen" is parsed as: CN=test, C=CA, O=foilen.
     * Input like "test" is parsed as: CN=test.
     */
    private List<String[]> parseSubjectString(String subjectString) {
        List<String[]> result = new ArrayList<>();
        String[] parts = subjectString.split(",");
        for (String part : parts) {
            part = part.trim();
            int eqIdx = part.indexOf('=');
            if (eqIdx < 0) {
                // No '=': treat as CN value
                result.add(new String[]{"CN", part});
            } else {
                String key = part.substring(0, eqIdx).trim();
                String value = part.substring(eqIdx + 1).trim();
                result.add(new String[]{key, value});
            }
        }
        return result;
    }

    /**
     * Map an attribute key abbreviation to its OID.
     */
    private String attributeKeyToOid(String key) {
        switch (key.toUpperCase()) {
            case "CN": return OID_COMMON_NAME;
            case "C":  return OID_COUNTRY;
            case "O":  return OID_ORGANIZATION;
            case "OU": return OID_ORG_UNIT;
            case "ST": return OID_STATE;
            case "L":  return OID_LOCALITY;
            default:
                throw new SmallToolsException("Unknown DN attribute key: " + key);
        }
    }

    private byte[] derPrintableString(String value) {
        try {
            return derTlv(0x13, value.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new SmallToolsException("ASCII not supported", e);
        }
    }

    private byte[] derValidity(Date notBefore, Date notAfter) {
        return derSequence(concat(derTime(notBefore), derTime(notAfter)));
    }

    private byte[] derAlgorithmIdentifierSHA256withRSA() {
        return derSequence(concat(derOid(OID_SHA256_WITH_RSA), derNull()));
    }

    private byte[] derExtensions(List<String> sanDns) {
        // subjectAltName extension
        byte[] sanOid = derOid(OID_SAN);

        // Build GeneralNames SEQUENCE content
        ByteArrayOutputStream sanContent = new ByteArrayOutputStream();
        for (String dns : sanDns) {
            // dNSName [2] IMPLICIT IA5String
            try {
                byte[] dnsBytes = dns.getBytes("US-ASCII");
                byte[] tagged = derTlv(0x82, dnsBytes);
                sanContent.write(tagged, 0, tagged.length);
            } catch (IOException e) {
                throw new SmallToolsException("Error encoding SAN", e);
            }
        }
        byte[] generalNames = derSequence(sanContent.toByteArray());
        byte[] sanOctetString = derOctetString(generalNames);

        byte[] extension = derSequence(concat(sanOid, sanOctetString));
        byte[] extensionsSeq = derSequence(extension);
        return extensionsSeq;
    }

    private byte[] derTlv(int tag, byte[] value) {
        byte[] lengthBytes = derLength(value.length);
        byte[] result = new byte[1 + lengthBytes.length + value.length];
        result[0] = (byte) tag;
        System.arraycopy(lengthBytes, 0, result, 1, lengthBytes.length);
        System.arraycopy(value, 0, result, 1 + lengthBytes.length, value.length);
        return result;
    }

    private byte[] derLength(int length) {
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

    private byte[] concat(byte[]... arrays) {
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

    // -----------------------------------------------------------------------
    // DN parsing helpers
    // -----------------------------------------------------------------------

    private String extractCN(String dn) {
        for (String part : splitDn(dn)) {
            part = part.trim();
            if (part.startsWith("CN=")) {
                return unescapeDnValue(part.substring(3));
            }
        }
        return null;
    }

    /**
     * Split a RFC 2253 DN string on commas that are not escaped.
     */
    private List<String> splitDn(String dn) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < dn.length(); i++) {
            char c = dn.charAt(i);
            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                current.append(c);
                escaped = true;
            } else if (c == ',') {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        return parts;
    }

    private String unescapeDnValue(String value) {
        return value.replace("\\,", ",").replace("\\+", "+").replace("\\\"", "\"")
                .replace("\\\\", "\\").replace("\\<", "<").replace("\\>", ">")
                .replace("\\;", ";");
    }

}
