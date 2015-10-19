/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.foilen.smalltools.crypt.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.cert.CertificateDetails;
import com.foilen.smalltools.crypt.cert.RSACertificate;
import com.foilen.smalltools.crypt.cert.RSATrustedCertificates;
import com.foilen.smalltools.exception.SmallToolsException;

public class CommanderSslTest {

    private RSACrypt rsaCrypt = new RSACrypt();
    private RSACertificate firstRoot;
    private RSACertificate secondRoot;

    private RSACertificate firstAlice;
    private RSACertificate secondMurray;
    private RSACertificate secondPaul;

    private RSACertificate selfTom;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public CommanderSslTest() {
        firstRoot = selfSign("firstRoot");
        secondRoot = selfSign("secondRoot");
        selfTom = selfSign("selfTom");

        firstAlice = nodeSign(firstRoot, "firstAlice");
        secondMurray = nodeSign(secondRoot, "secondMurray");
        secondPaul = nodeSign(secondRoot, "secondPaul");
    }

    private RSACertificate nodeSign(RSACertificate root, String commonName) {
        return root.signPublicKey(rsaCrypt.generateKeyPair(2048), new CertificateDetails().setCommonName(commonName));
    }

    private RSACertificate selfSign(String commonName) {
        RSACertificate cert = new RSACertificate(rsaCrypt.generateKeyPair(2048));
        return cert.selfSign(new CertificateDetails().setCommonName(commonName));
    }

    private void testCommandFail(RSACertificate serverCertificate, RSACertificate serverTrustCertificate, RSACertificate clientCertificate, RSACertificate clientTrustCertificate)
            throws InterruptedException {

        // Server
        CommanderServer commanderServer = new CommanderServer();
        commanderServer.setServerCertificate(serverCertificate);
        commanderServer.setClientTrustedCertificates(serverTrustCertificate == null ? null : new RSATrustedCertificates().addTrustedRsaCertificate(serverTrustCertificate));
        commanderServer.start();
        int port = commanderServer.getPort();
        Assert.assertNotEquals(0, port);

        // Client
        CommanderTest.countDownLatch = new CountDownLatch(1);
        CommanderClient commanderClient = new CommanderClient();
        commanderClient.setClientCertificate(clientCertificate);
        commanderClient.setServerTrustedCertificates(clientTrustCertificate == null ? null : new RSATrustedCertificates().addTrustedRsaCertificate(clientTrustCertificate));

        // Send one command
        boolean hadException = false;
        try {
            commanderClient.sendCommandAndWaitResponse("127.0.0.1", port, new CountDownCommandWithResponse("A"));
        } catch (SmallToolsException e) {
            hadException = true;
        }
        Assert.assertTrue(hadException);
        Assert.assertEquals(1, CommanderTest.countDownLatch.getCount());

        // Close
        commanderServer.stop();
    }

    private void testCommandSuccess(RSACertificate serverCertificate, RSACertificate serverTrustCertificate, RSACertificate clientCertificate, RSACertificate clientTrustCertificate)
            throws InterruptedException {
        // Server
        CommanderServer commanderServer = new CommanderServer();
        commanderServer.setServerCertificate(serverCertificate);
        commanderServer.setClientTrustedCertificates(serverTrustCertificate == null ? null : new RSATrustedCertificates().addTrustedRsaCertificate(serverTrustCertificate));
        commanderServer.start();
        int port = commanderServer.getPort();
        Assert.assertNotEquals(0, port);

        // Client
        CommanderTest.countDownLatch = new CountDownLatch(1);
        CommanderClient commanderClient = new CommanderClient();
        commanderClient.setClientCertificate(clientCertificate);
        commanderClient.setServerTrustedCertificates(clientTrustCertificate == null ? null : new RSATrustedCertificates().addTrustedRsaCertificate(clientTrustCertificate));

        // Send one command
        CustomResponse response = commanderClient.sendCommandAndWaitResponse("127.0.0.1", port, new CountDownCommandWithResponse("A"));
        CommanderTest.countDownLatch.await();
        Assert.assertEquals(1, commanderClient.getConnectionsCount());
        Assert.assertEquals("AA", response.getMsg());

        // Close
        commanderClient.closeConnection("127.0.0.1", port);
        commanderServer.stop();
        Assert.assertEquals(0, commanderClient.getConnectionsCount());
    }

    @Test(timeout = 10000)
    public void testSendACommandWithResponse_FailClientDoesntTrust() throws Exception {
        testCommandFail(firstAlice, secondRoot, secondMurray, secondRoot);
    }

    @Test(timeout = 10000)
    public void testSendACommandWithResponse_FailServerDoesntTrust() throws Exception {
        testCommandFail(firstAlice, firstRoot, secondMurray, firstRoot);
    }

    @Test(timeout = 10000)
    public void testSendACommandWithResponse_Success_TrustServerCert() throws Exception {
        testCommandSuccess(selfTom, secondRoot, secondPaul, selfTom);
    }

    @Test(timeout = 10000)
    public void testSendACommandWithResponse_SuccessTrustCA() throws Exception {
        testCommandSuccess(firstAlice, secondRoot, secondMurray, firstRoot);
    }

    @Test(timeout = 10000)
    public void testSendACommandWithResponse_SuccessTrustOnlyServer() throws Exception {
        testCommandSuccess(firstAlice, null, secondMurray, firstRoot);
    }
}
