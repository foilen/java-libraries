/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.crypt.bouncycastle.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.bouncycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.test.asserts.AssertTools;

public class RSACertificateTest {

    private RSACrypt rsaCrypt = new RSACrypt();

    private void assertCerts(RSACertificate expected, RSACertificate actual) {
        Assert.assertEquals(expected.getCommonName(), actual.getCommonName());
        Assert.assertEquals(expected.getThumbprint(), actual.getThumbprint());
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
    public void testIsValidSignature() {
        // Root
        AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
        AsymmetricKeyParameter rootPublicKey = rootKeys.getPublicKey();
        RSACertificate rootCertificate = new RSACertificate(rootKeys);
        rootCertificate.selfSign(new CertificateDetails().setCommonName("CA root").addSanDns("CA root SAN 1", "CA root SAN 2"));
        assertCommonNamesAndSans(rootCertificate, new String[] { "CA root" }, new String[] { "CA root SAN 1", "CA root SAN 2" });

        // Node
        AsymmetricKeys nodeKeys = rsaCrypt.generateKeyPair(2048);
        AsymmetricKeyParameter nodePublicKey = nodeKeys.getPublicKey();
        RSACertificate nodeCertificate = rootCertificate.signPublicKey(nodeKeys, new CertificateDetails().setCommonName("p001.node.foilen.org").addSanDns("P SAN 1", "P SAN 2"));
        assertCommonNamesAndSans(nodeCertificate, new String[] { "p001.node.foilen.org" }, new String[] { "P SAN 1", "P SAN 2" });

        // Node without san
        AsymmetricKeys nodeNoSanKeys = rsaCrypt.generateKeyPair(2048);
        RSACertificate nodeNoSanCertificate = rootCertificate.signPublicKey(nodeNoSanKeys, new CertificateDetails().setCommonName("p002.node.foilen.org"));
        assertCommonNamesAndSans(nodeNoSanCertificate, new String[] { "p002.node.foilen.org" }, new String[] {});

        // Fake Root
        AsymmetricKeys fakeRootKeys = rsaCrypt.generateKeyPair(2048);
        AsymmetricKeyParameter fakeRootPublicKey = fakeRootKeys.getPublicKey();
        RSACertificate fakeRootCertificate = new RSACertificate(fakeRootKeys);
        fakeRootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));

        // Assert certificates
        Assert.assertTrue(rootCertificate.isValidSignature(rootCertificate));
        Assert.assertTrue(nodeCertificate.isValidSignature(rootCertificate));
        Assert.assertTrue(fakeRootCertificate.isValidSignature(fakeRootCertificate));

        Assert.assertFalse(rootCertificate.isValidSignature(nodeCertificate));
        Assert.assertFalse(rootCertificate.isValidSignature(fakeRootCertificate));
        Assert.assertFalse(nodeCertificate.isValidSignature(nodeCertificate));
        Assert.assertFalse(nodeCertificate.isValidSignature(fakeRootCertificate));
        Assert.assertFalse(fakeRootCertificate.isValidSignature(rootCertificate));
        Assert.assertFalse(fakeRootCertificate.isValidSignature(nodeCertificate));

        // Assert key pair
        Assert.assertTrue(rootCertificate.isValidSignature(rootKeys));
        Assert.assertTrue(nodeCertificate.isValidSignature(rootKeys));
        Assert.assertTrue(fakeRootCertificate.isValidSignature(fakeRootKeys));

        Assert.assertFalse(rootCertificate.isValidSignature(nodeKeys));
        Assert.assertFalse(rootCertificate.isValidSignature(fakeRootKeys));
        Assert.assertFalse(nodeCertificate.isValidSignature(nodeKeys));
        Assert.assertFalse(nodeCertificate.isValidSignature(fakeRootKeys));
        Assert.assertFalse(fakeRootCertificate.isValidSignature(rootKeys));
        Assert.assertFalse(fakeRootCertificate.isValidSignature(nodeKeys));

        // Assert key
        Assert.assertTrue(rootCertificate.isValidSignature(rootPublicKey));
        Assert.assertTrue(nodeCertificate.isValidSignature(rootPublicKey));
        Assert.assertTrue(fakeRootCertificate.isValidSignature(fakeRootPublicKey));

        Assert.assertFalse(rootCertificate.isValidSignature(nodePublicKey));
        Assert.assertFalse(rootCertificate.isValidSignature(fakeRootPublicKey));
        Assert.assertFalse(nodeCertificate.isValidSignature(nodePublicKey));
        Assert.assertFalse(nodeCertificate.isValidSignature(fakeRootPublicKey));
        Assert.assertFalse(fakeRootCertificate.isValidSignature(rootPublicKey));
        Assert.assertFalse(fakeRootCertificate.isValidSignature(nodePublicKey));
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

        Assert.assertNull(loadedRootCertificate.getKeysForSigning().getPrivateKey());
        Assert.assertNotNull(loadedRootCertificate.getKeysForSigning().getPublicKey());
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

        Assert.assertFalse(certificatePem.contains("RSA PRIVATE KEY"));
        Assert.assertFalse(certificatePem.contains("PUBLIC KEY"));

        // Load
        RSACertificate loadedRootCertificate = RSACertificate.loadPemFromString(certificatePem, privateKeyPem, publicKeyPem, null);

        // Assert
        assertCerts(rootCertificate, loadedRootCertificate);
        Assert.assertNotNull(loadedRootCertificate.getKeysForSigning().getPrivateKey());
        Assert.assertNotNull(loadedRootCertificate.getKeysForSigning().getPublicKey());
    }

    @Test
    public void testTransformingToKeyManagerFactory() throws Exception {
        AsymmetricKeys keys = rsaCrypt.generateKeyPair(2048);
        RSACertificate certificate = new RSACertificate(keys);
        RSACertificate rsaCertificate = certificate.selfSign(new CertificateDetails().setCommonName("me"));

        KeyManagerFactory keyManagerFactory = RSATools.createKeyManagerFactory(rsaCertificate);

        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
        Assert.assertEquals(1, keyManagers.length);
        X509KeyManager keyManager = (X509KeyManager) keyManagers[0];
        Assert.assertNotNull(keyManager.getPrivateKey("me"));
        Assert.assertNull(keyManager.getPrivateKey("you"));

    }

}
