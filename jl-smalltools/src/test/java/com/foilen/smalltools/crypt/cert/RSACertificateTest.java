/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.cert;

import java.io.File;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;

import com.foilen.smalltools.crypt.asymmetric.AsymmetricKeys;
import com.foilen.smalltools.crypt.asymmetric.RSACrypt;

public class RSACertificateTest {

    private RSACrypt rsaCrypt = new RSACrypt();

    private void assertCerts(RSACertificate expected, RSACertificate actual) {
        Assert.assertEquals(expected.getCommonName(), actual.getCommonName());
        Assert.assertEquals(expected.getThumbprint(), actual.getThumbprint());
    }

    @Test
    public void testIsValidSignature() {

        // Root
        AsymmetricKeys rootKeys = rsaCrypt.generateKeyPair(2048);
        AsymmetricKeyParameter rootPublicKey = rootKeys.getPublicKey();
        RSACertificate rootCertificate = new RSACertificate(rootKeys);
        rootCertificate.selfSign(new CertificateDetails().setCommonName("CA root"));

        // Node
        AsymmetricKeys nodeKeys = rsaCrypt.generateKeyPair(2048);
        AsymmetricKeyParameter nodePublicKey = nodeKeys.getPublicKey();
        RSACertificate nodeCertificate = rootCertificate.signPublicKey(nodeKeys, new CertificateDetails().setCommonName("p001.node.foilen.org"));

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
