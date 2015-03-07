/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.bridge;

import java.net.Socket;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionAssemblyLine;
import com.foilen.smalltools.net.services.ExecutorServiceWrappedSocketCallback;
import com.foilen.smalltools.net.services.SocketCallback;
import com.foilen.smalltools.net.services.TCPServerService;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * This is the part that gets a connection from {@link ConnectionBridgeEntry} and reach out to an external service.
 * 
 * Usage:
 * 
 * <pre>
 * ConnectionBridgeExit connectionBridgeExit = new ConnectionBridgeExit();
 * 
 * // Local port to get calls from the ConnectionBridgeEntry (if you do not want to get a random one)
 * connectionBridgeExit.setServerPort(9999);
 * 
 * // The remote server to which we will relay the calls (you can also extend this class and override the contactRemote() method to have dynamic remote. E.g. retrieve the host/port from the socket)
 * connectionBridgeExit.setRemoteHostname(&quot;myBridge&quot;);
 * connectionBridgeExit.setRemotePort(9999);
 * 
 * // If you want to secure the connection to the bridge
 * ConnectionAssemblyLine connectionAssemblyLine = new ConnectionAssemblyLine();
 * connectionAssemblyLine.addAction(new CryptRsaAesConnectionAction());
 * connectionBridgeExit.setIncomingAssemblyLine(connectionAssemblyLine);
 * 
 * // Start receiving calls from the ConnectionBridgeEntry
 * connectionBridgeExit.initLocalServer();
 * </pre>
 */
public class ConnectionBridgeExit {

    private class ExitBridgeSocketCallback implements SocketCallback {

        @Override
        public void newClient(Socket bridgeEntrySocket) {

            Connection bridgeEntryConnection = new Connection(bridgeEntrySocket);
            Connection remoteConnection = null;
            try {

                // Initiate the dialog with the bridge
                bridgeEntryConnection = incomingAssemblyLine.process(bridgeEntryConnection);

                if (bridgeEntryConnection == null) {
                    return;
                }

                // Contact the remote server
                remoteConnection = new Connection(contactRemote(bridgeEntryConnection));

                // Start to relay
                Thread thread1 = StreamsTools.flowStreamNonBlocking(bridgeEntryConnection.getInputStream(), remoteConnection.getOutputStream());
                Thread thread2 = StreamsTools.flowStreamNonBlocking(remoteConnection.getInputStream(), bridgeEntryConnection.getOutputStream());

                // Wait for the end
                thread1.join();
                thread2.join();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bridgeEntryConnection.close();
                remoteConnection.close();
            }
        }

    }

    private ConnectionAssemblyLine incomingAssemblyLine = new ConnectionAssemblyLine();

    private Integer serverPort;

    private TCPServerService tcpServerService;

    protected String remoteHostname;
    protected int remotePort;

    /**
     * Open the socket to the remote service to contact. The default behavior is to use the values set by {@link #setRemoteHostname(String)} and {@link #setRemotePort(int)}. You can override this
     * method to have use a dynamic way.
     * 
     * @param bridgeEntryConnection
     *            the connection from the bridge if you need values coming from there
     * @return the socket to the final remote server
     */
    protected Socket contactRemote(Connection bridgeEntryConnection) {
        try {
            return new Socket(remoteHostname, remotePort);
        } catch (Exception e) {
            throw new SmallToolsException("Could not connect to remote host", e);
        }
    }

    public String getRemoteHostname() {
        return remoteHostname;
    }

    public int getRemotePort() {
        return remotePort;
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
            tcpServerService = new TCPServerService(new ExecutorServiceWrappedSocketCallback(new ExitBridgeSocketCallback()));
        } else {
            tcpServerService = new TCPServerService(serverPort, new ExecutorServiceWrappedSocketCallback(new ExitBridgeSocketCallback()));
        }

        return tcpServerService;

    }

    public void setRemoteHostname(String remoteHostname) {
        this.remoteHostname = remoteHostname;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * The local server port where the connections from ConnectionBridgeEntry get into.
     * 
     * @param serverPort
     *            the local server port
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public ConnectionAssemblyLine getIncomingAssemblyLine() {
        return incomingAssemblyLine;
    }

    public void setIncomingAssemblyLine(ConnectionAssemblyLine incomingAssemblyLine) {
        this.incomingAssemblyLine = incomingAssemblyLine;
    }

}
