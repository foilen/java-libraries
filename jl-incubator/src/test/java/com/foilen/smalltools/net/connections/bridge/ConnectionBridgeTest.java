/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.bridge;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.foilen.smalltools.net.connections.ConnectionAssemblyLine;
import com.foilen.smalltools.net.connections.actions.CryptRsaAesConnectionAction;
import com.foilen.smalltools.net.connections.actions.PasswordGateConnectionAction;
import com.foilen.smalltools.net.connections.bridge.ConnectionBridgeEntry;
import com.foilen.smalltools.net.connections.bridge.ConnectionBridgeExit;
import com.foilen.smalltools.net.services.TCPServerService;
import com.foilen.smalltools.tools.StreamsTools;

public class ConnectionBridgeTest {

    static private String expected;

    @BeforeClass
    static public void initExpected() throws MalformedURLException, IOException {
        String ip = InetAddress.getByName("foilen.com").getHostAddress();
        expected = StreamsTools.consumeAsString(new URL("http://" + ip).openStream());
        Assert.assertNotNull(expected);
        Assert.assertTrue(expected.length() > 20);
    }

    private int bridgePort;
    private ConnectionBridgeEntry connectionBridgeEntry;

    private ConnectionBridgeExit connectionBridgeExit;

    @Before
    public void initBridge() {
        // Create the bridge
        connectionBridgeEntry = new ConnectionBridgeEntry();
        connectionBridgeEntry.setBridgeExitHostname("127.0.0.1");
        TCPServerService entryServer = connectionBridgeEntry.initLocalServer();

        connectionBridgeExit = new ConnectionBridgeExit();
        connectionBridgeExit.setRemoteHostname("foilen.com");
        connectionBridgeExit.setRemotePort(80);
        TCPServerService exitServer = connectionBridgeExit.initLocalServer();

        connectionBridgeEntry.setBridgeExitport(exitServer.getPort());

        bridgePort = entryServer.getPort();
    }

    @Test(timeout = 5000)
    public void testCryptProxyBridge() throws Exception {

        // Protect
        ConnectionAssemblyLine assemblyLine = new ConnectionAssemblyLine();
        assemblyLine.addAction(new CryptRsaAesConnectionAction());

        connectionBridgeEntry.setOutgoingAssemblyLine(assemblyLine);
        connectionBridgeExit.setIncomingAssemblyLine(assemblyLine);

        // Call the bridge
        String actual = StreamsTools.consumeAsString(new URL("http://127.0.0.1:" + bridgePort).openStream());

        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000)
    public void testDirectProxyBridge() throws Exception {

        // Call the bridge
        String actual = StreamsTools.consumeAsString(new URL("http://127.0.0.1:" + bridgePort).openStream());

        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000)
    public void testPasswordAndCryptProxyBridge() throws Exception {

        // Protect
        ConnectionAssemblyLine assemblyLine = new ConnectionAssemblyLine();
        PasswordGateConnectionAction passwordAction = new PasswordGateConnectionAction();
        passwordAction.setPassword("myBridgePassword");
        assemblyLine.addAction(passwordAction);
        assemblyLine.addAction(new CryptRsaAesConnectionAction());

        connectionBridgeEntry.setOutgoingAssemblyLine(assemblyLine);
        connectionBridgeExit.setIncomingAssemblyLine(assemblyLine);

        // Call the bridge
        String actual = StreamsTools.consumeAsString(new URL("http://127.0.0.1:" + bridgePort).openStream());

        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000)
    public void testPasswordProxyBridge() throws Exception {

        // Protect
        ConnectionAssemblyLine assemblyLine = new ConnectionAssemblyLine();
        PasswordGateConnectionAction passwordAction = new PasswordGateConnectionAction();
        passwordAction.setPassword("myBridgePassword");
        assemblyLine.addAction(passwordAction);

        connectionBridgeEntry.setOutgoingAssemblyLine(assemblyLine);
        connectionBridgeExit.setIncomingAssemblyLine(assemblyLine);

        // Call the bridge
        String actual = StreamsTools.consumeAsString(new URL("http://127.0.0.1:" + bridgePort).openStream());

        Assert.assertEquals(expected, actual);
    }

}
