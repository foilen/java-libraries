package com.foilen.smalltools.crypt.jce.asymmetric;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.CollectionsTools;

import java.io.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA cryptography.
 * <p>
 * Default:
 * <ul>
 * <li>RSA: The cipher</li>
 * <li>ECB: Electronic Codebook Mode</li>
 * <li>PKCS1Padding: The padding algorithm</li>
 * </ul>
 * <p>
 * Usage:
 *
 * <pre>
 * // Prepare the message
 * String message = &quot;Hello World&quot;;
 * byte[] data = message.getBytes(StandardCharsets.UTF_8);
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

    /**
     * An instance of the crypt.
     */
    public static final RSACrypt RSA_CRYPT = new RSACrypt();

    @Override
    protected String getCipherTransformation() {
        return "RSA/ECB/PKCS1Padding";
    }

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
                asymmetricKeys.setPublicKey(keyDetails.getJcaPublicKey());
            }

            if (privateExponent != null) {
                asymmetricKeys.setPrivateKey(keyDetails.getJcaPrivateKey());
            }

            return asymmetricKeys;

        } catch (Exception e) {
            throw new SmallToolsException("Could not create the keys", e);
        }
    }

    @Override
    public AsymmetricKeys generateKeyPair(int keysize) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keysize, random);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return new AsymmetricKeys(keyPair.getPublic(), keyPair.getPrivate());
        } catch (Exception e) {
            throw new SmallToolsException("Could not generate key pair", e);
        }
    }

    @Override
    public AsymmetricKeys loadKeysPemFromString(String... pems) {
        RSAKeyDetails keyDetails = new RSAKeyDetails();
        try {
            for (String pem : pems) {
                if (pem == null) {
                    continue;
                }

                // Parse all PEM blocks in this string
                BufferedReader reader = new BufferedReader(new StringReader(pem));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.equals("-----BEGIN RSA PRIVATE KEY-----")) {
                        // PKCS#1 private key
                        String base64 = readPemBody(reader, "-----END RSA PRIVATE KEY-----");
                        byte[] der = Base64.getMimeDecoder().decode(base64);
                        RSAKeyDetails parsed = decodePkcs1PrivateKey(der);
                        keyDetails.setModulus(parsed.getModulus());
                        keyDetails.setPrivateExponent(parsed.getPrivateExponent());
                        keyDetails.setPublicExponent(parsed.getPublicExponent());
                        if (parsed.isCrt()) {
                            keyDetails.setCrt(true);
                            keyDetails.setPrimeP(parsed.getPrimeP());
                            keyDetails.setPrimeQ(parsed.getPrimeQ());
                            keyDetails.setPrimeExponentP(parsed.getPrimeExponentP());
                            keyDetails.setPrimeExponentQ(parsed.getPrimeExponentQ());
                            keyDetails.setCrtCoefficient(parsed.getCrtCoefficient());
                        }
                    } else if (line.equals("-----BEGIN PUBLIC KEY-----")) {
                        // X.509/SPKI public key
                        String base64 = readPemBody(reader, "-----END PUBLIC KEY-----");
                        byte[] der = Base64.getMimeDecoder().decode(base64);
                        KeyFactory kf = KeyFactory.getInstance("RSA");
                        RSAPublicKey rsaPublicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(der));
                        keyDetails.setModulus(rsaPublicKey.getModulus());
                        keyDetails.setPublicExponent(rsaPublicKey.getPublicExponent());
                    }
                }
            }
            return createKeyPair(keyDetails);
        } catch (Exception e) {
            throw new SmallToolsException("Problem loading the keys", e);
        }
    }

    /**
     * Read lines until the end marker, returning the concatenated base64 content.
     */
    private String readPemBody(BufferedReader reader, String endMarker) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.equals(endMarker)) {
                break;
            }
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Decode a PKCS#1 RSAPrivateKey DER structure into an {@link RSAKeyDetails}.
     * <p>
     * SEQUENCE {
     * INTEGER version (0)
     * INTEGER modulus
     * INTEGER publicExponent
     * INTEGER privateExponent
     * INTEGER prime1
     * INTEGER prime2
     * INTEGER exponent1
     * INTEGER exponent2
     * INTEGER coefficient
     * }
     */
    private RSAKeyDetails decodePkcs1PrivateKey(byte[] der) {
        DerParser parser = new DerParser(der);
        DerParser seq = parser.readSequence();
        seq.readInteger(); // version (must be 0, skip it)
        BigInteger modulus = seq.readInteger();
        BigInteger publicExponent = seq.readInteger();
        BigInteger privateExponent = seq.readInteger();
        BigInteger prime1 = seq.readInteger();
        BigInteger prime2 = seq.readInteger();
        BigInteger exponent1 = seq.readInteger();
        BigInteger exponent2 = seq.readInteger();
        BigInteger coefficient = seq.readInteger();

        RSAKeyDetails details = new RSAKeyDetails();
        details.setModulus(modulus);
        details.setPublicExponent(publicExponent);
        details.setPrivateExponent(privateExponent);
        if (CollectionsTools.isAnyItemNotNull(prime1, prime2, exponent1, exponent2, coefficient)) {
            details.setCrt(true);
            details.setPrimeP(prime1);
            details.setPrimeQ(prime2);
            details.setPrimeExponentP(exponent1);
            details.setPrimeExponentQ(exponent2);
            details.setCrtCoefficient(coefficient);
        }
        return details;
    }

    @Override
    public RSAKeyDetails retrieveKeyDetails(AsymmetricKeys keyPair) {

        RSAKeyDetails rsaKeyDetails = new RSAKeyDetails();

        try {
            // Public key
            if (keyPair.getPublicKey() != null) {
                RSAPublicKey rsaKey = (RSAPublicKey) keyPair.getPublicKey();
                rsaKeyDetails.setModulus(rsaKey.getModulus());
                rsaKeyDetails.setPublicExponent(rsaKey.getPublicExponent());
            }

            // Private key
            if (keyPair.getPrivateKey() != null) {
                if (keyPair.getPrivateKey() instanceof RSAPrivateCrtKey rsaPrivateCrtKey) {
                    rsaKeyDetails.setModulus(rsaPrivateCrtKey.getModulus());
                    rsaKeyDetails.setPrivateExponent(rsaPrivateCrtKey.getPrivateExponent());
                    rsaKeyDetails.setCrt(true);
                    rsaKeyDetails.setPrimeP(rsaPrivateCrtKey.getPrimeP());
                    rsaKeyDetails.setPrimeQ(rsaPrivateCrtKey.getPrimeQ());
                    rsaKeyDetails.setPrimeExponentP(rsaPrivateCrtKey.getPrimeExponentP());
                    rsaKeyDetails.setPrimeExponentQ(rsaPrivateCrtKey.getPrimeExponentQ());
                    rsaKeyDetails.setCrtCoefficient(rsaPrivateCrtKey.getCrtCoefficient());

                    // Derive publicExponent from private key if not already set from public key
                    if (rsaKeyDetails.getPublicExponent() == null) {
                        rsaKeyDetails.setPublicExponent(rsaPrivateCrtKey.getPublicExponent());
                    }
                } else {
                    // Non-CRT
                    java.security.interfaces.RSAPrivateKey rsaPrivateKey = (java.security.interfaces.RSAPrivateKey) keyPair.getPrivateKey();
                    rsaKeyDetails.setModulus(rsaPrivateKey.getModulus());
                    rsaKeyDetails.setPrivateExponent(rsaPrivateKey.getPrivateExponent());
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
        try (Writer writer = new FileWriter(fileName)) {
            writePkcs1PrivateKeyPem(keyPair, writer);
            writePublicKeyPem(keyPair, writer);
        } catch (Exception e) {
            throw new SmallToolsException("Could not save keys", e);
        }
    }

    @Override
    public void savePrivateKeyPem(AsymmetricKeys keyPair, Writer writer) {
        AssertTools.assertNotNull(keyPair, "The keypair needs to be set to save it");
        AssertTools.assertNotNull(keyPair.getPrivateKey(), "The private key needs to be set to save it");
        try {
            writePkcs1PrivateKeyPem(keyPair, writer);
        } catch (Exception e) {
            throw new SmallToolsException("Could not save key", e);
        } finally {
            CloseableTools.close(writer);
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
        try {
            writePublicKeyPem(keyPair, writer);
        } catch (Exception e) {
            throw new SmallToolsException("Could not save key", e);
        } finally {
            CloseableTools.close(writer);
        }
    }

    /**
     * Write private key as PKCS#1 PEM ("RSA PRIVATE KEY") using manual DER encoding.
     */
    private void writePkcs1PrivateKeyPem(AsymmetricKeys keyPair, Writer writer) throws IOException {
        RSAKeyDetails keyDetails = retrieveKeyDetails(keyPair);
        byte[] der = encodePkcs1PrivateKey(keyDetails);
        writePem(writer, "RSA PRIVATE KEY", der);
    }

    /**
     * Write public key as SPKI PEM ("PUBLIC KEY").
     */
    private void writePublicKeyPem(AsymmetricKeys keyPair, Writer writer) throws IOException {
        byte[] encoded = keyPair.getPublicKey().getEncoded(); // X.509/SPKI DER
        writePem(writer, "PUBLIC KEY", encoded);
    }

    /**
     * Write a PEM block to the writer (does NOT close the writer).
     */
    private void writePem(Writer writer, String type, byte[] der) throws IOException {
        writer.write("-----BEGIN " + type + "-----\n");
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(der);
        writer.write(base64);
        writer.write("\n-----END " + type + "-----\n");
        writer.flush();
    }

    /**
     * Encode an RSA private key to PKCS#1 DER format.
     */
    private byte[] encodePkcs1PrivateKey(RSAKeyDetails keyDetails) {
        // Collect the integers
        byte[] version = derInteger(BigInteger.ZERO);
        byte[] modulus = derInteger(keyDetails.getModulus());
        byte[] publicExponent = derInteger(keyDetails.getPublicExponent() != null ? keyDetails.getPublicExponent() : BigInteger.ZERO);
        byte[] privateExponent = derInteger(keyDetails.getPrivateExponent());
        byte[] prime1 = derInteger(keyDetails.getPrimeP() != null ? keyDetails.getPrimeP() : BigInteger.ZERO);
        byte[] prime2 = derInteger(keyDetails.getPrimeQ() != null ? keyDetails.getPrimeQ() : BigInteger.ZERO);
        byte[] exponent1 = derInteger(keyDetails.getPrimeExponentP() != null ? keyDetails.getPrimeExponentP() : BigInteger.ZERO);
        byte[] exponent2 = derInteger(keyDetails.getPrimeExponentQ() != null ? keyDetails.getPrimeExponentQ() : BigInteger.ZERO);
        byte[] coefficient = derInteger(keyDetails.getCrtCoefficient() != null ? keyDetails.getCrtCoefficient() : BigInteger.ZERO);

        byte[] content = concat(version, modulus, publicExponent, privateExponent, prime1, prime2, exponent1, exponent2, coefficient);
        return derSequence(content);
    }

    private byte[] derInteger(BigInteger value) {
        byte[] valueBytes = value.toByteArray();
        return derTlv(0x02, valueBytes);
    }

    private byte[] derSequence(byte[] content) {
        return derTlv(0x30, content);
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

    /**
     * Minimal DER parser for reading PKCS#1 RSA private key structures.
     */
    private static class DerParser {
        private final byte[] data;
        private int pos;

        DerParser(byte[] data) {
            this.data = data;
            this.pos = 0;
        }

        DerParser readSequence() {
            int tag = data[pos++] & 0xFF;
            if (tag != 0x30) {
                throw new SmallToolsException("Expected SEQUENCE tag 0x30, got 0x" + Integer.toHexString(tag));
            }
            int length = readLength();
            byte[] content = new byte[length];
            System.arraycopy(data, pos, content, 0, length);
            pos += length;
            return new DerParser(content);
        }

        BigInteger readInteger() {
            int tag = data[pos++] & 0xFF;
            if (tag != 0x02) {
                throw new SmallToolsException("Expected INTEGER tag 0x02, got 0x" + Integer.toHexString(tag));
            }
            int length = readLength();
            byte[] value = new byte[length];
            System.arraycopy(data, pos, value, 0, length);
            pos += length;
            return new BigInteger(value);
        }

        private int readLength() {
            int first = data[pos++] & 0xFF;
            if (first < 0x80) {
                return first;
            }
            int numBytes = first & 0x7F;
            int length = 0;
            for (int i = 0; i < numBytes; i++) {
                length = (length << 8) | (data[pos++] & 0xFF);
            }
            return length;
        }
    }

}
