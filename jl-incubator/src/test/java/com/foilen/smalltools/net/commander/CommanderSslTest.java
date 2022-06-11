/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.foilen.smalltools.crypt.spongycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.spongycastle.cert.CertificateDetails;
import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.crypt.spongycastle.cert.RSATrustedCertificates;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;

public class CommanderSslTest {

    private RSACrypt rsaCrypt = new RSACrypt();
    private RSACertificate firstRoot;
    private RSACertificate firstRootBis;
    private RSACertificate secondRoot;

    private RSACertificate firstAlice;
    private RSACertificate firstAliceBis;
    private RSACertificate secondMurray;
    private RSACertificate secondPaul;

    private RSACertificate selfTom;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public CommanderSslTest() {
        firstRoot = selfSign("firstRoot");
        firstRootBis = selfSign("firstRoot");
        secondRoot = selfSign("secondRoot");
        selfTom = selfSign("selfTom");

        firstAlice = nodeSign(firstRoot, "firstAlice");
        firstAliceBis = nodeSign(firstRootBis, "firstAlice");
        secondMurray = nodeSign(secondRoot, "secondMurray");
        secondPaul = nodeSign(secondRoot, "secondPaul");
    }

    private CommanderConnection grabRemoteConnection(CommanderConnection commanderConnection) throws InterruptedException {
        GrabRemoteConnectionCommand.reset();
        GrabRemoteConnectionCommand grab = new GrabRemoteConnectionCommand();
        commanderConnection.sendCommand(grab);
        GrabRemoteConnectionCommand.waitForRun();
        return GrabRemoteConnectionCommand.getCommanderConnection();
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
            CommanderConnection commanderConnection = commanderClient.getCommanderConnection("127.0.0.1", port);
            commanderConnection.sendCommandAndWaitResponse(new CountDownCommandWithResponse("A"));
        } catch (SmallToolsException e) {
            hadException = true;
        }
        Assert.assertTrue(hadException);
        Assert.assertEquals(1, CommanderTest.countDownLatch.getCount());

        // Close
        commanderServer.stop();
    }

    private void testCommandSuccess(RSACertificate serverCertificate, List<RSACertificate> serverTrustCertificates, RSACertificate clientCertificate, List<RSACertificate> clientTrustCertificates)
            throws Exception {
        // Server
        CommanderServer commanderServer = new CommanderServer();
        commanderServer.setServerCertificate(serverCertificate);
        commanderServer.setClientTrustedCertificates(serverTrustCertificates == null ? null : new RSATrustedCertificates().addTrustedRsaCertificate(serverTrustCertificates));
        commanderServer.start();
        int port = commanderServer.getPort();
        Assert.assertNotEquals(0, port);

        // Client
        CommanderTest.countDownLatch = new CountDownLatch(1);
        CommanderClient commanderClient = new CommanderClient();
        commanderClient.setClientCertificate(clientCertificate);
        commanderClient.setServerTrustedCertificates(clientTrustCertificates == null ? null : new RSATrustedCertificates().addTrustedRsaCertificate(clientTrustCertificates));

        // Send one command
        CommanderConnection commanderConnection = commanderClient.getCommanderConnection("127.0.0.1", port);
        CustomResponse response = commanderConnection.sendCommandAndWaitResponse(new CountDownCommandWithResponse("A"));
        CommanderTest.countDownLatch.await();
        Assert.assertEquals(1, commanderClient.getConnectionsCount());
        Assert.assertEquals("AA", response.getMsg());

        // Check that the remote connection does not have the server's port to call back
        CommanderConnection remoteConnectionOnServerSide = grabRemoteConnection(commanderConnection);
        Assert.assertNotEquals((Integer) port, remoteConnectionOnServerSide.getPort());

        // Get the remote certificate
        List<RSACertificate> remoteCertificatesOnServerSide = remoteConnectionOnServerSide.getPeerSslCertificates();
        if (clientCertificate == null || serverTrustCertificates == null) {
            Assert.assertNull(remoteCertificatesOnServerSide);
        } else {
            Assert.assertNotNull(remoteCertificatesOnServerSide);
            Assert.assertEquals(1, remoteCertificatesOnServerSide.size());
            Assert.assertEquals(clientCertificate.getCommonName(), remoteCertificatesOnServerSide.get(0).getCommonName());
        }

        // Close
        commanderClient.closeConnection("127.0.0.1", port);
        commanderServer.stop();
        Assert.assertEquals(0, commanderClient.getConnectionsCount());
    }

    private void testCommandSuccess(RSACertificate serverCertificate, RSACertificate serverTrustCertificate, RSACertificate clientCertificate, RSACertificate clientTrustCertificate) throws Exception {
        testCommandSuccess( //
                serverCertificate, //
                serverTrustCertificate == null ? null : Collections.singletonList(serverTrustCertificate), //
                clientCertificate, //
                clientTrustCertificate == null ? null : Collections.singletonList(clientTrustCertificate) //
        );
    }

    @Test(timeout = 60000)
    public void testPeer2Peer() throws Throwable {

        // Clients
        CommanderClient commanderClientA = new CommanderClient();
        commanderClientA.setClientCertificate(secondMurray);
        commanderClientA.setServerTrustedCertificates(new RSATrustedCertificates().addTrustedRsaCertificate(secondRoot));

        CommanderClient commanderClientB = new CommanderClient();
        commanderClientB.setClientCertificate(secondPaul);
        commanderClientB.setServerTrustedCertificates(new RSATrustedCertificates().addTrustedRsaCertificate(secondRoot));

        // Servers
        CommanderServer commanderServerA = new CommanderServer();
        commanderServerA.setServerCertificate(secondMurray);
        commanderServerA.setClientTrustedCertificates(new RSATrustedCertificates().addTrustedRsaCertificate(secondRoot));
        commanderServerA.setCommanderClient(commanderClientA);
        commanderClientA.setCommanderServer(commanderServerA);
        commanderServerA.start();
        int portA = commanderServerA.getPort();
        Assert.assertNotEquals(0, portA);

        CommanderServer commanderServerB = new CommanderServer();
        commanderServerB.setServerCertificate(secondPaul);
        commanderServerB.setClientTrustedCertificates(new RSATrustedCertificates().addTrustedRsaCertificate(secondRoot));
        commanderServerB.setCommanderClient(commanderClientB);
        commanderClientB.setCommanderServer(commanderServerB);
        commanderServerB.start();
        int portB = commanderServerB.getPort();
        Assert.assertNotEquals(0, portB);

        // Connect from A to B
        CommanderConnection commanderConnectionCaSb = commanderClientA.getCommanderConnection("127.0.0.1", portB);
        Assert.assertEquals("127.0.0.1", commanderConnectionCaSb.getHost());
        Assert.assertEquals((Integer) portB, commanderConnectionCaSb.getPort());
        Assert.assertTrue(commanderConnectionCaSb.isConnected());

        // Check that the remote connection has the server's port to call back
        CommanderConnection remoteConnection = grabRemoteConnection(commanderConnectionCaSb);
        Assert.assertEquals((Integer) portA, remoteConnection.getPort());

        // Send a command (connection aware), close the connection, send the response (will connect to the server)
        CloseChannelThenReconnectCommand.reset();
        CloseChannelThenReconnectCommand grab = new CloseChannelThenReconnectCommand();
        commanderConnectionCaSb.sendCommand(grab);
        Throwable throwable = CloseChannelThenReconnectCommand.waitForRun();
        if (throwable != null) {
            throw throwable;
        }
    }

    @Test(timeout = 60000)
    public void testSendACommandWithResponse_FailClientDoesntTrust() throws Exception {
        testCommandFail(firstAlice, secondRoot, secondMurray, secondRoot);
    }

    @Test(timeout = 60000)
    public void testSendACommandWithResponse_FailServerDoesntTrust() throws Exception {
        testCommandFail(firstAlice, firstRoot, secondMurray, firstRoot);
    }

    @Test(timeout = 60000)
    public void testSendACommandWithResponse_Success_TrustServerCert() throws Exception {
        testCommandSuccess(selfTom, secondRoot, secondPaul, selfTom);
    }

    @Test(timeout = 60000)
    public void testSendACommandWithResponse_SuccessTrustCA() throws Exception {
        testCommandSuccess(firstAlice, secondRoot, secondMurray, firstRoot);
    }

    @Test(timeout = 60000)
    public void testSendACommandWithResponse_SuccessTrustCA_SameCA_Name() throws Exception {
        testCommandSuccess(firstAlice, Arrays.asList(firstRoot, firstRootBis), firstAliceBis, Arrays.asList(firstRoot, firstRootBis));
    }

    @Test(timeout = 60000)
    public void testSendACommandWithResponse_SuccessTrustOnlyServer() throws Exception {
        testCommandSuccess(firstAlice, null, secondMurray, firstRoot);
    }

}
