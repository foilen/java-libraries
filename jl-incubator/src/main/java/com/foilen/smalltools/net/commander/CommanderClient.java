/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.crypt.spongycastle.cert.RSATrustedCertificates;
import com.foilen.smalltools.net.commander.channel.CommanderDecoder;
import com.foilen.smalltools.net.commander.channel.CommanderEncoder;
import com.foilen.smalltools.net.commander.channel.CommanderExecutionChannel;
import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.internal.LocalServerPortCommand;
import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;
import com.foilen.smalltools.net.commander.connectionpool.ConnectionPool;
import com.foilen.smalltools.net.commander.connectionpool.SimpleConnectionPool;
import com.foilen.smalltools.net.netty.NettyBuilder;
import com.foilen.smalltools.net.netty.NettyClient;

/**
 * The client side of the commander system. See {@link CommanderServer} for all the details and sample usage.
 */
public class CommanderClient {

    private RSATrustedCertificates serverTrustedCertificates;
    private RSACertificate clientCertificate;

    private boolean configureSpring;
    private CommanderServer commanderServer;

    private ConnectionPool connectionPool = new SimpleConnectionPool();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Close all the connections.
     */
    public void closeAllConnections() {
        connectionPool.closeAllConnections();
    }

    /**
     * If the connection pool keeps a connection open, it can close it.
     *
     * @param host
     *            the host name
     * @param port
     *            the port
     */
    public void closeConnection(String host, int port) {
        connectionPool.closeConnection(host, port);
    }

    /**
     * INTERNAL: This is used by the {@link ConnectionPool}. Use the send*() methods instead.
     *
     * @param host
     *            the host to connect to
     * @param port
     *            the port to connect to
     * @return the {@link NettyClient}
     */
    public NettyClient createNettyClient(final String host, final int port) {

        NettyBuilder nettyBuilder = new NettyBuilder();
        nettyBuilder.setCertificate(clientCertificate);
        nettyBuilder.setTrustedCertificates(serverTrustedCertificates);
        nettyBuilder.addChannelHandler(CommanderDecoder.class);
        nettyBuilder.addChannelHandler(CommanderExecutionChannel.class, configureSpring, this, executorService);
        nettyBuilder.addChannelHandler(CommanderEncoder.class);

        NettyClient nettyClient = nettyBuilder.buildClient(host, port);

        // Send the local server's port
        if (commanderServer != null) {
            nettyClient.writeFlush(new LocalServerPortCommand(commanderServer.getPort()));
        }

        return nettyClient;
    }

    /**
     * Get the client certificate.
     *
     * @return the client certificate
     */
    public RSACertificate getClientCertificate() {
        return clientCertificate;
    }

    /**
     * Call this to get a connection where to send messages.
     *
     * @param host
     *            the host name
     * @param port
     *            the port
     * @return the connection
     */
    public CommanderConnection getCommanderConnection(String host, int port) {
        return connectionPool.getConnection(this, host, port);
    }

    /**
     * Get the local server that could receive connections from this client.
     *
     * @return the commander server
     */
    public CommanderServer getCommanderServer() {
        return commanderServer;
    }

    /**
     * Call this to get a connection where to send messages.
     *
     * @param host
     *            the host name
     * @param port
     *            the port
     * @return the connection
     */
    public CommanderConnection getConnection(String host, int port) {
        return connectionPool.getConnection(this, host, port);
    }

    /**
     * Tells how many connections are opened.
     *
     * @return the number of connections
     */
    public int getConnectionsCount() {
        return connectionPool.getConnectionsCount();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Get the certificates that are trusted to connect to.
     *
     * @return the trusted certificates
     */
    public RSATrustedCertificates getServerTrustedCertificates() {
        return serverTrustedCertificates;
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
     * Set the client certificate.
     *
     * @param clientCertificate
     *            the client certificate
     * @return this
     */
    public CommanderClient setClientCertificate(RSACertificate clientCertificate) {
        this.clientCertificate = clientCertificate;
        return this;
    }

    /**
     * Set the commander server that could receive connections from this client. This is useful if you want to tell the server how to connect back to this machine if the connection is broken. (When
     * the client connects, it will send its server port to configure the remote connection. This is only when the client also has a server. Kind of a Peer to peer)
     *
     * @param commanderServer
     *            the commander server to let the remote machine reconnect to closed outgoing connections
     */
    public void setCommanderServer(CommanderServer commanderServer) {
        this.commanderServer = commanderServer;
    }

    /**
     * Set if you want all the deserialized objects to be filled by Spring.
     *
     * @param configureSpring
     *            true to configure the {@link CommandImplementation} (e.g: fill the @Autowired)
     * @return this
     */
    public CommanderClient setConfigureSpring(boolean configureSpring) {
        this.configureSpring = configureSpring;
        return this;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Set the certificates that are trusted to connect to.
     *
     * @param serverTrustedCertificates
     *            the trusted certificates
     * @return this
     */
    public CommanderClient setServerTrustedCertificates(RSATrustedCertificates serverTrustedCertificates) {
        this.serverTrustedCertificates = serverTrustedCertificates;
        return this;
    }

}
