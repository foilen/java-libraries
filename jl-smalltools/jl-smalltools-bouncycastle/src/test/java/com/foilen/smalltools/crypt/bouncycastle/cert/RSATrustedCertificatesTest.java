/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.foilen.smalltools.crypt.bouncycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.tools.DateTools;

public class RSATrustedCertificatesTest {

    private static final int KEY_SIZE = 1024;

    private RSACertificate certA;
    private RSACertificate certABis;
    private RSACertificate certAB;
    private RSACertificate certAB_Exp;
    private RSACertificate certABC;
    private RSACertificate certM;
    private RSACertificate certMN;
    private RSACertificate certMNO;
    private RSACertificate certX_Exp;
    private RSACertificate certXY;

    private void assertInvalid(RSATrustedCertificates rsaTrustedCertificates, RSACertificate... rsaCertificates) {
        for (RSACertificate rsaCertificate : rsaCertificates) {
            String errorMessage = "Certificate " + rsaCertificate.getCommonName() + " is trusted";
            Assert.assertFalse(errorMessage, rsaTrustedCertificates.isTrusted(rsaCertificate));
        }
    }

    private void assertValid(RSATrustedCertificates rsaTrustedCertificates, RSACertificate... rsaCertificates) {
        for (RSACertificate rsaCertificate : rsaCertificates) {
            String errorMessage = "Certificate " + rsaCertificate.getCommonName() + " is not trusted";
            Assert.assertTrue(errorMessage, rsaTrustedCertificates.isTrusted(rsaCertificate));
        }
    }

    private void assertValidWithIntermediates(RSATrustedCertificates rsaTrustedCertificates, RSACertificate intermediate, RSACertificate... rsaCertificates) {
        for (RSACertificate rsaCertificate : rsaCertificates) {
            String errorMessage = "Certificate " + rsaCertificate.getCommonName() + " is not trusted";
            Assert.assertTrue(errorMessage, rsaTrustedCertificates.isTrusted(rsaCertificate, intermediate));
        }
    }

    @Before
    public void before() {

        Date now = new Date();
        Date lastYear = DateTools.addDate(now, Calendar.YEAR, -1);
        Date lastLastYear = DateTools.addDate(now, Calendar.YEAR, -2);

        RSACrypt rsaCrypt = new RSACrypt();
        certA = new RSACertificate(rsaCrypt.generateKeyPair(KEY_SIZE)).selfSign(new CertificateDetails().setCommonName("A"));
        certABis = new RSACertificate(rsaCrypt.generateKeyPair(KEY_SIZE)).selfSign(new CertificateDetails().setCommonName("A"));
        certAB = certA.signPublicKey(rsaCrypt.generateKeyPair(KEY_SIZE), new CertificateDetails().setCommonName("AB"));
        certAB_Exp = certA.signPublicKey(rsaCrypt.generateKeyPair(KEY_SIZE), new CertificateDetails().setCommonName("AB_Exp").setStartDate(lastLastYear).setEndDate(lastYear));
        certABC = certAB.signPublicKey(rsaCrypt.generateKeyPair(KEY_SIZE), new CertificateDetails().setCommonName("ABC"));

        certM = new RSACertificate(rsaCrypt.generateKeyPair(KEY_SIZE)).selfSign(new CertificateDetails().setCommonName("M"));
        certMN = certM.signPublicKey(rsaCrypt.generateKeyPair(KEY_SIZE), new CertificateDetails().setCommonName("MN"));
        certMNO = certMN.signPublicKey(rsaCrypt.generateKeyPair(KEY_SIZE), new CertificateDetails().setCommonName("MNO"));

        certX_Exp = new RSACertificate(rsaCrypt.generateKeyPair(KEY_SIZE)).selfSign(new CertificateDetails().setCommonName("X_Exp").setStartDate(lastLastYear).setEndDate(lastYear));
        certXY = certX_Exp.signPublicKey(rsaCrypt.generateKeyPair(KEY_SIZE), new CertificateDetails().setCommonName("X_ExpY"));
    }

    @Test
    public void testIsValid() throws Exception {

        // None trusted
        RSATrustedCertificates rsaTrustedCertificates = new RSATrustedCertificates();
        assertInvalid(rsaTrustedCertificates, certA, certAB, certAB_Exp, certABC, certM, certMN, certX_Exp, certXY);

        // A is trusted ; without intermediates
        rsaTrustedCertificates.addTrustedRsaCertificate(certA);
        assertValid(rsaTrustedCertificates, certA, certAB);
        assertInvalid(rsaTrustedCertificates, certAB_Exp, certABC, certM, certMN, certX_Exp, certXY);
        assertValidWithIntermediates(rsaTrustedCertificates, certAB, certA, certAB, certABC);

        // A is trusted ; without intermediates (with 2 CA with the same subject)
        rsaTrustedCertificates.addTrustedRsaCertificate(certABis);
        assertValid(rsaTrustedCertificates, certA, certAB);
        assertInvalid(rsaTrustedCertificates, certAB_Exp, certABC, certM, certMN, certX_Exp, certXY);
        assertValidWithIntermediates(rsaTrustedCertificates, certAB, certA, certAB, certABC);

        // A is trusted ; with permanent intermediate AB
        rsaTrustedCertificates.addIntermediateRsaCertificate(certAB);
        assertValid(rsaTrustedCertificates, certA, certAB, certABC);
        assertInvalid(rsaTrustedCertificates, certAB_Exp, certM, certMN, certX_Exp, certXY);

        // A and M are trusted ; with permanent intermediate AB
        rsaTrustedCertificates.addTrustedRsaCertificate(certM);
        assertValid(rsaTrustedCertificates, certA, certAB, certABC, certM, certMN);
        assertInvalid(rsaTrustedCertificates, certAB_Exp, certX_Exp, certXY);

        // A, M and X_Exp are trusted ; with permanent intermediate AB
        rsaTrustedCertificates.addTrustedRsaCertificate(certX_Exp);
        assertValid(rsaTrustedCertificates, certA, certAB, certABC, certM, certMN);
        assertInvalid(rsaTrustedCertificates, certAB_Exp, certX_Exp, certXY);

        // Load from file
        File file = File.createTempFile("junit", null);
        certA.saveCertificatePem(file.getAbsolutePath());
        rsaTrustedCertificates = new RSATrustedCertificates();
        assertInvalid(rsaTrustedCertificates, certA, certAB, certAB_Exp, certABC, certM, certMN, certX_Exp, certXY);

        rsaTrustedCertificates.addTrustedFromPemFile(file.getAbsolutePath());
        assertValid(rsaTrustedCertificates, certA, certAB);
        assertInvalid(rsaTrustedCertificates, certAB_Exp, certABC, certM, certMN, certX_Exp, certXY);
    }

    @Test
    public void testTransformingToKeyStore() throws Exception {
        RSATrustedCertificates rsaTrustedCertificates = new RSATrustedCertificates();
        rsaTrustedCertificates.addTrustedRsaCertificate(certA);
        rsaTrustedCertificates.addTrustedRsaCertificate(certM);
        rsaTrustedCertificates.addIntermediateRsaCertificate(certAB);

        KeyStore keyStore = RSATools.createKeyStore(rsaTrustedCertificates);

        Assert.assertTrue(keyStore.containsAlias(certA.getThumbprint()));
        Assert.assertTrue(keyStore.containsAlias(certM.getThumbprint()));
        Assert.assertFalse(keyStore.containsAlias(certAB.getThumbprint()));
    }

    @Test
    public void testTransformingToTrustManagerFactory() throws Exception {
        RSATrustedCertificates rsaTrustedCertificates = new RSATrustedCertificates();
        rsaTrustedCertificates.addTrustedRsaCertificate(certA);
        rsaTrustedCertificates.addTrustedRsaCertificate(certM);
        rsaTrustedCertificates.addIntermediateRsaCertificate(certAB);

        TrustManagerFactory trustManagerFactory = RSATools.createTrustManagerFactory(rsaTrustedCertificates);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        Assert.assertEquals(1, trustManagers.length);
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

        // Try success (with intermediate)
        X509Certificate[] chain = new X509Certificate[2];
        chain[0] = certAB.getCertificate();
        chain[1] = certABC.getCertificate();
        String authType = "RSA";
        trustManager.checkServerTrusted(chain, authType);

        // Try success (with provided intermediate)
        chain = new X509Certificate[1];
        chain[0] = certABC.getCertificate();
        trustManager.checkServerTrusted(chain, authType);

        // Try fail (not provided intermediate)
        boolean hadException = false;
        try {
            chain = new X509Certificate[2];
            chain[0] = certABC.getCertificate();
            chain[1] = certMNO.getCertificate();
            trustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            hadException = true;
        }
        Assert.assertTrue(hadException);

    }

}
