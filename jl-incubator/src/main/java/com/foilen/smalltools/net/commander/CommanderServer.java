/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.crypt.cert.RSACertificate;
import com.foilen.smalltools.crypt.cert.RSATools;
import com.foilen.smalltools.crypt.cert.RSATrustedCertificates;
import com.foilen.smalltools.net.commander.channel.CommanderDecoder;
import com.foilen.smalltools.net.commander.channel.CommanderEncoder;
import com.foilen.smalltools.net.commander.channel.CommanderExecutionChannel;
import com.foilen.smalltools.net.commander.command.AbstractCommandRequest;
import com.foilen.smalltools.net.commander.command.AbstractCommandRequestWithResponse;
import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandRequest;
import com.foilen.smalltools.tools.AssertTools;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;

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
 * CommanderServer commanderServer = new CommanderServer().setPort(9999);
 * commanderServer.setServerCertificate(serverCert);
 * commanderServer.setClientTrustedCertificates(new RSATrustedCertificates().addTrustedRsaCertificate(caCert)); // Don't set if you do not want to authenticate the clients
 * commanderServer.start();
 * 
 * CommanderClient commanderClient = new CommanderClient();
 * commanderClient.setClientCertificate(clientCert); // Don't set if you do not want to authenticate the client
 * commanderClient.setServerTrustedCertificates(new RSATrustedCertificates().addTrustedRsaCertificate(caCert));
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
 * <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * compile 'org.springframework:spring-beans:4.1.6.RELEASE' (optional)
 * </pre>
 */
public class CommanderServer {

    private static final Logger logger = LoggerFactory.getLogger(CommanderServer.class);

    private RSATrustedCertificates clientTrustedCertificates;
    private RSACertificate serverCertificate;

    private boolean configureSpring;

    private Thread thread;

    private int port;

    /**
     * Get the certificates that are trusted to receive connections from.
     * 
     * @return the trusted certificates
     */
    public RSATrustedCertificates getClientTrustedCertificates() {
        return clientTrustedCertificates;
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
     * Waits for this thread to die.
     * 
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    public void join() throws InterruptedException {
        AssertTools.assertNotNull(thread, "Server is not started");
        thread.join();
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

        AssertTools.assertNull(thread, "Server already started");

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup incomingEventLoopGroup = new NioEventLoopGroup();
                EventLoopGroup requestsEventLoopGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap //
                            .group(incomingEventLoopGroup, requestsEventLoopGroup)//
                            .channel(NioServerSocketChannel.class)//
                            .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            InetSocketAddress remoteAddress = socketChannel.remoteAddress();
                            logger.info("Got a connection from {}:{}", remoteAddress.getHostName(), remoteAddress.getPort());

                            // Add sslCtx if needed
                            if (clientTrustedCertificates != null || serverCertificate != null) {
                                TrustManagerFactory trustManagerFactory = clientTrustedCertificates == null ? null : RSATools.createTrustManagerFactory(clientTrustedCertificates);
                                KeyManagerFactory keyManagerFactory = serverCertificate == null ? null : RSATools.createKeyManagerFactory(serverCertificate);

                                CipherSuiteFilter cipherFilter = IdentityCipherSuiteFilter.INSTANCE;
                                SslContext sslCtx = SslContext.newServerContext(SslProvider.JDK, null, trustManagerFactory, null, null, null, keyManagerFactory, null, cipherFilter, null, 0, 0);
                                SslHandler sslHandler = sslCtx.newHandler(socketChannel.alloc());

                                if (trustManagerFactory == null) {
                                    logger.debug("Will not verify client's identity");
                                } else {
                                    logger.debug("Will verify client's identity");
                                    SSLEngine sslEngine = sslHandler.engine();
                                    sslEngine.setNeedClientAuth(true);
                                }

                                socketChannel.pipeline().addLast(sslHandler);
                            }

                            // Add the commander encoder and decoder
                            socketChannel.pipeline().addLast(new CommanderDecoder());
                            socketChannel.pipeline().addLast(new CommanderExecutionChannel(configureSpring));
                            socketChannel.pipeline().addLast(new CommanderEncoder());
                        }
                    }) //
                            .option(ChannelOption.SO_BACKLOG, 128) //
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    logger.info("Server on port {} is starting...", port);
                    ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
                    SocketAddress socketAddress = channelFuture.channel().localAddress();
                    if (socketAddress instanceof InetSocketAddress) {
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                        port = inetSocketAddress.getPort();
                    }
                    logger.info("Server on port {} is started", port);
                    countDownLatch.countDown();
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    logger.info("Server on port {} is interrupted", port);
                } finally {
                    requestsEventLoopGroup.shutdownGracefully();
                    incomingEventLoopGroup.shutdownGracefully();
                    countDownLatch.countDown();
                }
                logger.info("Server on port {} is stopped", port);
            }
        }, "Commander Server");
        thread.start();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for the server to start");
        }

        return this;
    }

    /**
     * Request the server to stop.
     */
    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
    }
}
