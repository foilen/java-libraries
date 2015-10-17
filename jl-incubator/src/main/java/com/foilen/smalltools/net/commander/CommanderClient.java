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
import com.foilen.smalltools.net.commander.command.AbstractCommandWithResponse;
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
 * </pre>
 */
public class CommanderClient {

    private static final Logger logger = LoggerFactory.getLogger(CommanderClient.class);

    private RSATrustedCertificates serverTrustedCertificates;
    private RSACertificate clientCertificate;

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
     * INTERNAL: This is used by the {@link ConnectionPool}. Use the send* methods instead.
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
                    socketChannel.pipeline().addLast(new CommanderDecoder());
                    socketChannel.pipeline().addLast(new CommanderExecutionChannel());
                    socketChannel.pipeline().addLast(new CommanderEncoder());
                }
            });

            logger.info("Connecting to {}:{}", host, port);
            final ChannelFuture result = bootstrap.connect(host, port).sync();
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
     * Send a command to a server or connected client.
     * 
     * @param host
     *            the host name
     * @param port
     *            the port
     * @param command
     *            the command to run
     */
    public void sendCommand(String host, int port, Runnable command) {
        connectionPool.sendCommand(this, host, port, command);
    }

    /**
     * Send a command to a server or connected client and wait for the response.
     * 
     * @param host
     *            the host name
     * @param port
     *            the port
     * @param commandWithReply
     *            the command to run
     * @param <R>
     *            the response type
     * @return the response
     */
    public <R> R sendCommandAndWaitResponse(String host, int port, AbstractCommandWithResponse<R> commandWithReply) {
        return connectionPool.sendCommandAndWaitResponse(this, host, port, commandWithReply);
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
