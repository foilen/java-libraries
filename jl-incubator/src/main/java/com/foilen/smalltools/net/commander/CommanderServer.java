/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.foilen.smalltools.crypt.cert.RSACertificate;
import com.foilen.smalltools.crypt.cert.RSATrustedCertificates;
import com.foilen.smalltools.net.commander.channel.CommanderDecoder;
import com.foilen.smalltools.net.commander.channel.CommanderEncoder;
import com.foilen.smalltools.net.commander.channel.CommanderExecutionChannel;
import com.foilen.smalltools.net.commander.command.AbstractCommandRequest;
import com.foilen.smalltools.net.commander.command.AbstractCommandRequestWithResponse;
import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandRequest;
import com.foilen.smalltools.net.netty.NettyBuilder;
import com.foilen.smalltools.net.netty.NettyServer;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CloseableTools;

/**
 * This is a server/client system using Netty to easily create a TCP service that can be encrypted and authenticated (both ways) using TSL/SSL. Once connected, it is possible to send
 * {@link CommandRequest}, {@link AbstractCommandRequest} or {@link AbstractCommandRequestWithResponse} to send one-way commands or a request that needs a response.
 * 
 * <pre>
 * Details:
 * - Can use SSL to encrypt and optionally authenticate the server and/or the client
 * - Can configure (@Autowired) the deserialized objects with Spring ({@link #setConfigureSpring(boolean)}) 
 * - The exchange protocol is:
 * - - Sends the Class name of the object sent
 * - - Sends the JSON representation of the object (to send the parameters)
 * - On the other side, it is:
 * - - Creating the object from the Class name
 * - - Fill the object values by deserializing the JSON
 * - - Executes the {@link CommandImplementation}
 * </pre>
 * 
 * Usage without encryption:
 * 
 * <pre>
 * // Server and client
 * CommanderServer commanderServer = new CommanderServer().setPort(9999).start();
 * CommanderClient commanderClient = new CommanderClient();
 * 
 * // One-way message (sent async)
 * commanderClient.sendCommand("localhost", 9999, new EchoCommand("Hello World"));
 * 
 * // Request waiting for the response (will throw an exception if the connection is closed while waiting)
 * String hello = commanderClient.sendCommandAndWaitResponse("localhost", 9999, new HelloCommand("Bob"));
 * System.out.println("Got this hello response: " + hello);
 * 
 * // Close the connection when done with it
 * commanderClient.closeConnection("localhost", 9999);
 * </pre>
 * 
 * Usage with encryption (TSL/SSL):
 * 
 * <pre>
 * // Generate certificates from one certificate authority and use them
 * RSACrypt rsaCrypt = new RSACrypt();
 * RSACertificate caCert = new RSACertificate(rsaCrypt.generateKeyPair(2048)).selfSign(new CertificateDetails().setCommonName("The CA"));
 * RSACertificate serverCert = caCert.signPublicKey(rsaCrypt.generateKeyPair(2048), new CertificateDetails().setCommonName("The server"));
 * RSACertificate clientCert = caCert.signPublicKey(rsaCrypt.generateKeyPair(2048), new CertificateDetails().setCommonName("The client"));
 * 
 * // Server and client
 * CommanderClient commanderClient = new CommanderClient();
 * commanderClient.setClientCertificate(clientCert); // Don't set if you do not want to authenticate the client
 * commanderClient.setServerTrustedCertificates(new RSATrustedCertificates().addTrustedRsaCertificate(caCert));
 * 
 * CommanderServer commanderServer = new CommanderServer().setPort(9999);
 * commanderServer.setServerCertificate(serverCert);
 * commanderServer.setClientTrustedCertificates(new RSATrustedCertificates().addTrustedRsaCertificate(caCert)); // Don't set if you do not want to authenticate the clients
 * 
 * // (optional) If you want to consider the client and server as a peer to peer that can connects to each other, you can set the following.
 * commanderServer.setCommanderClient(commanderClient);
 * commanderClient.setCommanderServer(commanderServer);
 * 
 * // Start the server
 * commanderServer.start();
 * 
 * // One-way message (sent async)
 * CommanderConnection commanderConnection = commanderClient.getCommanderConnection("localhost", 9999);
 * commanderConnection.sendCommand(new EchoCommand("Hello World"));
 * 
 * // Request waiting for the response (will throw an exception if the connection is closed while waiting)
 * String hello = commanderConnection.sendCommandAndWaitResponse(new HelloCommand("Bob"));
 * System.out.println("Got this hello response: " + hello);
 * 
 * // Close the connection when done with it
 * commanderClient.closeConnection("localhost", 9999);
 * </pre>
 * 
 * <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * compile 'org.springframework:spring-beans:4.1.6.RELEASE' (optional)
 * </pre>
 */
public class CommanderServer {

    private RSATrustedCertificates clientTrustedCertificates;
    private RSACertificate serverCertificate;

    private boolean configureSpring;
    private CommanderClient commanderClient;

    private NettyServer nettyServer;

    private int port;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Get the certificates that are trusted to receive connections from.
     * 
     * @return the trusted certificates
     */
    public RSATrustedCertificates getClientTrustedCertificates() {
        return clientTrustedCertificates;
    }

    /**
     * Get the local client that could connect to this server.
     * 
     * @return the commander client
     */
    public CommanderClient getCommanderClient() {
        return commanderClient;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Get the set port or the port on which it is binded.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the server certificate.
     * 
     * @return the server certificate
     */
    public RSACertificate getServerCertificate() {
        return serverCertificate;
    }

    /**
     * Get if you want all the deserialized objects to be filled by Spring.
     * 
     * @return true to configure the {@link CommandImplementation} (e.g: fill the @Autowired)
     */
    public boolean isConfigureSpring() {
        return configureSpring;
    }

    /**
     * Waits for this server to die.
     * 
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    public void join() throws InterruptedException {
        AssertTools.assertNotNull(nettyServer, "Server is not started");
        nettyServer.join();
    }

    /**
     * Set the certificates that are trusted to receive connections from.
     * 
     * @param clientTrustedCertificates
     *            the trusted certificates
     * @return this
     */
    public CommanderServer setClientTrustedCertificates(RSATrustedCertificates clientTrustedCertificates) {
        this.clientTrustedCertificates = clientTrustedCertificates;
        return this;
    }

    /**
     * Set the commander client that could connect to this server. This is useful if you want incoming commands to respond back to the client even if the connection is broken. (By trying to reconnect
     * to the client's server with this CommanderClient. This is only when the client also has a server. Kind of a Peer to peer)
     * 
     * @param commanderClient
     *            the commander client to reconnect to closed incoming connections
     */
    public void setCommanderClient(CommanderClient commanderClient) {
        this.commanderClient = commanderClient;
    }

    /**
     * Set if you want all the deserialized objects to be filled by Spring.
     * 
     * @param configureSpring
     *            true to configure the {@link CommandImplementation} (e.g: fill the @Autowired)
     * @return this
     */
    public CommanderServer setConfigureSpring(boolean configureSpring) {
        this.configureSpring = configureSpring;
        return this;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Choose which port to use.
     * 
     * @param port
     *            the port (0 for any ; then get it with {@link #getPort()})
     * @return this
     */
    public CommanderServer setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set the server certificate.
     * 
     * @param serverCertificate
     *            the server certificate
     * @return this
     */
    public CommanderServer setServerCertificate(RSACertificate serverCertificate) {
        this.serverCertificate = serverCertificate;
        return this;
    }

    /**
     * Starts the server.
     * 
     * @return this
     */
    public CommanderServer start() {

        AssertTools.assertNull(nettyServer, "Server already started");

        NettyBuilder nettyBuilder = new NettyBuilder();
        nettyBuilder.setCertificate(serverCertificate);
        nettyBuilder.setTrustedCertificates(clientTrustedCertificates);
        nettyBuilder.addChannelHandler(CommanderDecoder.class);
        nettyBuilder.addChannelHandler(CommanderExecutionChannel.class, configureSpring, commanderClient, executorService);
        nettyBuilder.addChannelHandler(CommanderEncoder.class);

        nettyServer = nettyBuilder.buildServer(port);
        port = nettyServer.getPort();

        return this;
    }

    /**
     * Request the server to stop.
     */
    public void stop() {
        CloseableTools.close(nettyServer);
    }
}
