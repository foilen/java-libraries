/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.crypt.cert.RSACertificate;
import com.foilen.smalltools.crypt.cert.RSATools;
import com.foilen.smalltools.crypt.cert.RSATrustedCertificates;
import com.foilen.smalltools.net.commander.channel.CommanderDecoder;
import com.foilen.smalltools.net.commander.channel.CommanderEncoder;
import com.foilen.smalltools.net.commander.channel.CommanderExecutionChannel;
import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.internal.LocalServerPortCommand;
import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;
import com.foilen.smalltools.net.commander.connectionpool.ConnectionPool;
import com.foilen.smalltools.net.commander.connectionpool.SimpleConnectionPool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;

/**
 * The client side of the commander system. See {@link CommanderServer} for all the details and sample usage.
 * 
 * <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * compile 'org.springframework:spring-beans:4.1.6.RELEASE' (optional)
 * </pre>
 */
public class CommanderClient {

    private static final Logger logger = LoggerFactory.getLogger(CommanderClient.class);

    private RSATrustedCertificates serverTrustedCertificates;
    private RSACertificate clientCertificate;

    private boolean configureSpring;
    private CommanderServer commanderServer;

    private ConnectionPool connectionPool = new SimpleConnectionPool();

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
     * @return the {@link ChannelFuture}
     */
    public ChannelFuture createChannelFuture(final String host, final int port) {
        final EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

            final CommanderDecoder commanderDecoder = new CommanderDecoder();
            final CommanderExecutionChannel commanderExecutionChannel = new CommanderExecutionChannel(configureSpring, this);
            final CommanderEncoder commanderEncoder = new CommanderEncoder();

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {

                    // Add sslCtx if needed
                    if (serverTrustedCertificates != null || clientCertificate != null) {
                        TrustManagerFactory trustManagerFactory = serverTrustedCertificates == null ? null : RSATools.createTrustManagerFactory(serverTrustedCertificates);
                        KeyManagerFactory keyManagerFactory = clientCertificate == null ? null : RSATools.createKeyManagerFactory(clientCertificate);

                        CipherSuiteFilter cipherFilter = IdentityCipherSuiteFilter.INSTANCE;
                        SslContext sslCtx = SslContext.newClientContext(SslProvider.JDK, null, trustManagerFactory, null, null, null, keyManagerFactory, null, cipherFilter, null, 0, 0);
                        socketChannel.pipeline().addLast(sslCtx.newHandler(socketChannel.alloc()));
                    }

                    // Add the commander encoder and decoder
                    socketChannel.pipeline().addLast(commanderDecoder);
                    socketChannel.pipeline().addLast(commanderExecutionChannel);
                    socketChannel.pipeline().addLast(commanderEncoder);

                }
            });

            logger.info("Connecting to {}:{}", host, port);
            final ChannelFuture result = bootstrap.connect(host, port).sync();

            // Send the local server's port
            if (commanderServer != null) {
                result.channel().writeAndFlush(new LocalServerPortCommand(commanderServer.getPort()));
            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        result.channel().closeFuture().sync();
                    } catch (InterruptedException e) {
                        logger.info("Client {}:{} is interrupted", host, port);
                    } finally {
                        workerGroup.shutdownGracefully();
                    }
                    logger.info("Client {}:{} is stopped", host, port);
                }
            });
            thread.setDaemon(true);
            thread.start();

            return result;
        } catch (InterruptedException e) {
            logger.info("Connection to {}:{} was interrupted while being created", host, port);
        }

        return null;
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
