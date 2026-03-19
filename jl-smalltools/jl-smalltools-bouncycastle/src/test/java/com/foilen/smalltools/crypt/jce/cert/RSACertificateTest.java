package com.foilen.smalltools.crypt.jce.cert;

import com.foilen.smalltools.crypt.jce.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.jce.asymmetric.RSACrypt;
import com.foilen.smalltools.test.asserts.AssertTools;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RSACertificateTest {

    private RSACrypt rsaCrypt = new RSACrypt();

    private void assertCerts(RSACertificate expected, RSACertificate actual) {
        Assertions.assertEquals(expected.getCommonName(), actual.getCommonName());
        Assertions.assertEquals(expected.getThumbprint(), actual.getThumbprint());
    }

    private void assertCommonNamesAndSans(RSACertificate certificate, String[] expectedCommonNames, String[] expectedSans) {
        // Common names
        List<String> expected = Arrays.asList(expectedCommonNames);
        List<String> actual = certificate.getCommonNames().stream().sorted().collect(Collectors.toList());
        AssertTools.assertJsonComparison(expected, actual);

        // Sans
        expected = Arrays.asList(expectedSans);
        actual = certificate.getSubjectAltNames().stream().sorted().collect(Collectors.toList());
        AssertTools.assertJsonComparison(expected, actual);
    }

    @Test
    public void testCommonNameWithSubject() throws Exception {
        File fileNode = File.createTempFile("junit", null);

        // Node
        AsymmetricKeys nodeKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate nodeCertificate = new RSACertificate(nodeKeys);
        nodeCertificate.selfSign(new CertificateDetails().setCommonName("test,C=CA,O=foilen,OU=unit,L=city,ST=state"));
        nodeCertificate.saveCertificatePem(fileNode.getAbsolutePath());

        // Load
        RSACertificate loadedNodeCertificate = RSACertificate.loadPemFromFile(fileNode.getAbsolutePath());

        // Assert
        Assertions.assertEquals("test", loadedNodeCertificate.getCommonName());
        Assertions.assertEquals("ST=state, L=city, OU=unit, O=foilen, C=CA, CN=test", loadedNodeCertificate.getCertificate().getSubjectX500Principal().toString());
    }

    @Test
    public void testIsValidSignature() {
        // Root
        AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
        AsymmetricKeyParameter rootPublicKey = rootKeys.getPublicKey();
        RSACertificate rootCertificate = new RSACertificate(rootKeys);
        rootCertificate.selfSign(new CertificateDetails().setCommonName("CA root").addSanDns("CA root SAN 1", "CA root SAN 2"));
        assertCommonNamesAndSans(rootCertificate, new String[]{"CA root"}, new String[]{"CA root SAN 1", "CA root SAN 2"});

        // Node
        AsymmetricKeys nodeKeys = rsaCrypt.generateKeyPair(2048);
        AsymmetricKeyParameter nodePublicKey = nodeKeys.getPublicKey();
        RSACertificate nodeCertificate = rootCertificate.signPublicKey(nodeKeys, new CertificateDetails().setCommonName("p001.node.foilen.org").addSanDns("P SAN 1", "P SAN 2"));
        assertCommonNamesAndSans(nodeCertificate, new String[]{"p001.node.foilen.org"}, new String[]{"P SAN 1", "P SAN 2"});

        // Node without san
        AsymmetricKeys nodeNoSanKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate nodeNoSanCertificate = rootCertificate.signPublicKey(nodeNoSanKeys, new CertificateDetails().setCommonName("p002.node.foilen.org"));
        assertCommonNamesAndSans(nodeNoSanCertificate, new String[]{"p002.node.foilen.org"}, new String[]{});

        // Fake Root
        AsymmetricKeys fakeRootKeys = rsaCrypt.generateKeyPair(2048);
        AsymmetricKeyParameter fakeRootPublicKey = fakeRootKeys.getPublicKey();
        RSACertificate fakeRootCertificate = new RSACertificate(fakeRootKeys);
        fakeRootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));

        // Assert certificates
        Assertions.assertTrue(rootCertificate.isValidSignature(rootCertificate));
        Assertions.assertTrue(nodeCertificate.isValidSignature(rootCertificate));
        Assertions.assertTrue(fakeRootCertificate.isValidSignature(fakeRootCertificate));

        Assertions.assertFalse(rootCertificate.isValidSignature(nodeCertificate));
        Assertions.assertFalse(rootCertificate.isValidSignature(fakeRootCertificate));
        Assertions.assertFalse(nodeCertificate.isValidSignature(nodeCertificate));
        Assertions.assertFalse(nodeCertificate.isValidSignature(fakeRootCertificate));
        Assertions.assertFalse(fakeRootCertificate.isValidSignature(rootCertificate));
        Assertions.assertFalse(fakeRootCertificate.isValidSignature(nodeCertificate));

        // Assert key pair
        Assertions.assertTrue(rootCertificate.isValidSignature(rootKeys));
        Assertions.assertTrue(nodeCertificate.isValidSignature(rootKeys));
        Assertions.assertTrue(fakeRootCertificate.isValidSignature(fakeRootKeys));

        Assertions.assertFalse(rootCertificate.isValidSignature(nodeKeys));
        Assertions.assertFalse(rootCertificate.isValidSignature(fakeRootKeys));
        Assertions.assertFalse(nodeCertificate.isValidSignature(nodeKeys));
        Assertions.assertFalse(nodeCertificate.isValidSignature(fakeRootKeys));
        Assertions.assertFalse(fakeRootCertificate.isValidSignature(rootKeys));
        Assertions.assertFalse(fakeRootCertificate.isValidSignature(nodeKeys));

        // Assert key
        Assertions.assertTrue(rootCertificate.isValidSignature(rootPublicKey));
        Assertions.assertTrue(nodeCertificate.isValidSignature(rootPublicKey));
        Assertions.assertTrue(fakeRootCertificate.isValidSignature(fakeRootPublicKey));

        Assertions.assertFalse(rootCertificate.isValidSignature(nodePublicKey));
        Assertions.assertFalse(rootCertificate.isValidSignature(fakeRootPublicKey));
        Assertions.assertFalse(nodeCertificate.isValidSignature(nodePublicKey));
        Assertions.assertFalse(nodeCertificate.isValidSignature(fakeRootPublicKey));
        Assertions.assertFalse(fakeRootCertificate.isValidSignature(rootPublicKey));
        Assertions.assertFalse(fakeRootCertificate.isValidSignature(nodePublicKey));
    }

    @Test
    public void testSaveAndLoadPem() throws Exception {

        File fileRoot = File.createTempFile("junit", null);
        File fileNode = File.createTempFile("junit", null);

        // Root
        AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate rootCertificate = new RSACertificate(rootKeys);
        rootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));
        rootCertificate.saveCertificatePem(fileRoot.getAbsolutePath());

        // Node
        AsymmetricKeys nodeKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate nodeCertificate = rootCertificate.signPublicKey(nodeKeys, new CertificateDetails().setCommonName("p001.node.foilen.org"));
        nodeCertificate.saveCertificatePem(fileNode.getAbsolutePath());

        // Load
        RSACertificate loadedRootCertificate = RSACertificate.loadPemFromFile(fileRoot.getAbsolutePath());
        RSACertificate loadedNodeCertificate = RSACertificate.loadPemFromFile(fileNode.getAbsolutePath());

        // Assert
        assertCerts(rootCertificate, loadedRootCertificate);
        assertCerts(nodeCertificate, loadedNodeCertificate);

        Assertions.assertNull(loadedRootCertificate.getKeysForSigning().getPrivateKey());
        Assertions.assertNotNull(loadedRootCertificate.getKeysForSigning().getPublicKey());
    }

    @Test
    public void testSaveAndLoadPem_SeparateStrings() throws Exception {

        // Root
        AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate rootCertificate = new RSACertificate(rootKeys);
        rootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));
        String certificatePem = rootCertificate.saveCertificatePemAsString();
        String privateKeyPem = RSACrypt.RSA_CRYPT.savePrivateKeyPemAsString(rootCertificate.getKeysForSigning());
        String publicKeyPem = RSACrypt.RSA_CRYPT.savePublicKeyPemAsString(rootCertificate.getKeysForSigning());

        Assertions.assertFalse(certificatePem.contains("RSA PRIVATE KEY"));
        Assertions.assertFalse(certificatePem.contains("PUBLIC KEY"));

        // Load
        RSACertificate loadedRootCertificate = RSACertificate.loadPemFromString(certificatePem, privateKeyPem, publicKeyPem, null);

        // Assert
        assertCerts(rootCertificate, loadedRootCertificate);
        Assertions.assertNotNull(loadedRootCertificate.getKeysForSigning().getPrivateKey());
        Assertions.assertNotNull(loadedRootCertificate.getKeysForSigning().getPublicKey());
    }

    @Test
    public void testTransformingToKeyManagerFactory() throws Exception {
        AsymmetricKeys keys = rsaCrypt.generateKeyPair(2048);
        RSACertificate certificate = new RSACertificate(keys);
        RSACertificate rsaCertificate = certificate.selfSign(new CertificateDetails().setCommonName("me"));

        KeyManagerFactory keyManagerFactory = RSATools.createKeyManagerFactory(rsaCertificate);

        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
        Assertions.assertEquals(1, keyManagers.length);
        X509KeyManager keyManager = (X509KeyManager) keyManagers[0];
        Assertions.assertNotNull(keyManager.getPrivateKey("me"));
        Assertions.assertNull(keyManager.getPrivateKey("you"));

    }

}
