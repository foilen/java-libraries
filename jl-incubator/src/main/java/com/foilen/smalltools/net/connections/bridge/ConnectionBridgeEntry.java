/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.bridge;

import java.net.Socket;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionAssemblyLine;
import com.foilen.smalltools.net.services.ExecutorServiceWrappedSocketCallback;
import com.foilen.smalltools.net.services.SocketCallback;
import com.foilen.smalltools.net.services.TCPServerService;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * This is the part that gets an incoming connection and connects to the {@link ConnectionBridgeExit}.
 * 
 * Usage:
 * 
 * <pre>
 * ConnectionBridgeEntry connectionBridgeEntry = new ConnectionBridgeEntry();
 * 
 * // Local port to get calls (if you do not want to get a random one)
 * connectionBridgeEntry.setServerPort(9000);
 * 
 * // The remote Bridge Exit to which we will relay the calls
 * connectionBridgeEntry.setBridgeExitHostname(&quot;myBridge&quot;);
 * connectionBridgeEntry.setBridgeExitport(9999);
 * 
 * // If you want to secure the connection to the bridge
 * ConnectionAssemblyLine connectionAssemblyLine = new ConnectionAssemblyLine();
 * connectionAssemblyLine.addAction(new CryptRsaAesConnectionAction());
 * connectionBridgeEntry.setOutgoingAssemblyLine(connectionAssemblyLine);
 * 
 * // Start receiving calls
 * connectionBridgeEntry.initLocalServer();
 * </pre>
 */
public class ConnectionBridgeEntry {

    private class EntryBridgeSocketCallback implements SocketCallback {

        @Override
        public void newClient(Socket entrySocket) {

            Connection entryConnection = new Connection(entrySocket);
            Connection bridgeExitConnection = null;
            try {

                // Connect to the ConnectionBridgeExit
                bridgeExitConnection = new Connection(bridgeExitHostname, bridgeExitport);
                bridgeExitConnection = outgoingAssemblyLine.process(bridgeExitConnection);

                if (bridgeExitConnection == null) {
                    return;
                }

                // Start to relay
                Thread thread1 = StreamsTools.flowStreamNonBlocking(entryConnection.getInputStream(), bridgeExitConnection.getOutputStream());
                Thread thread2 = StreamsTools.flowStreamNonBlocking(bridgeExitConnection.getInputStream(), entryConnection.getOutputStream());

                // Wait for the end
                thread1.join();
                thread2.join();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                entryConnection.close();
                bridgeExitConnection.close();
            }
        }

    }

    private ConnectionAssemblyLine outgoingAssemblyLine = new ConnectionAssemblyLine();

    private Integer serverPort;

    private TCPServerService tcpServerService;

    private String bridgeExitHostname;
    private int bridgeExitport;

    public String getBridgeExitHostname() {
        return bridgeExitHostname;
    }

    public int getBridgeExitport() {
        return bridgeExitport;
    }

    public ConnectionAssemblyLine getOutgoingAssemblyLine() {
        return outgoingAssemblyLine;
    }

    public int getServerPort() {
        return serverPort;
    }

    public TCPServerService getTcpServerService() {
        return tcpServerService;
    }

    public TCPServerService initLocalServer() {
        AssertTools.assertNull(tcpServerService, "The server is already activated");

        if (serverPort == null) {
            tcpServerService = new TCPServerService(new ExecutorServiceWrappedSocketCallback(new EntryBridgeSocketCallback()));
        } else {
            tcpServerService = new TCPServerService(serverPort, new ExecutorServiceWrappedSocketCallback(new EntryBridgeSocketCallback()));
        }

        return tcpServerService;

    }

    public void setBridgeExitHostname(String bridgeExitHostname) {
        this.bridgeExitHostname = bridgeExitHostname;
    }

    public void setBridgeExitport(int bridgeExitport) {
        this.bridgeExitport = bridgeExitport;
    }

    public void setOutgoingAssemblyLine(ConnectionAssemblyLine outgoingAssemblyLine) {
        this.outgoingAssemblyLine = outgoingAssemblyLine;
    }

    /**
     * The local server port where the connections gets into the bridge.
     * 
     * @param serverPort
     *            the local server port
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

}
