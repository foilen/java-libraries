/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.netty;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.crypt.cert.RSACertificate;
import com.foilen.smalltools.crypt.cert.RSATools;
import com.foilen.smalltools.crypt.cert.RSATrustedCertificates;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.AssertTools;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;

/**
 * A server created with {@link NettyBuilder}.
 */
public class NettyServer implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private Thread thread;

    private int bindedPort;

    /**
     * Stop and close the server.
     */
    @Override
    public void close() throws IOException {
        if (thread != null) {
            logger.info("Closing server listening on ", bindedPort);
            thread.interrupt();
        }
        thread = null;
    }

    /**
     * Get the port that it is currently listening on.
     * 
     * @return the port
     */
    public int getPort() {
        return bindedPort;
    }

    /**
     * Wait for this server to die.
     * 
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    public void join() throws InterruptedException {
        AssertTools.assertNotNull(thread, "Server is not started");
        thread.join();
    }

    /**
     * Start the server.
     * 
     * @param port
     *            the port to listen on (0 for a random port ; get it with {@link #getPort()})
     * @param trustedCertificates
     *            (optional) the certificate to trust connections from
     * @param certificate
     *            (optional) the server's certificate
     * @param channelHandlerContainers
     *            the channel handlers for the incoming connections
     */
    public void start(final int port, final RSATrustedCertificates trustedCertificates, final RSACertificate certificate, final List<ChannelHandlerContainer> channelHandlerContainers) {

        AssertTools.assertNull(thread, "Server is already started");

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(NettyCommon.EVENT_LOOP_GROUP, NettyCommon.EVENT_LOOP_GROUP);
                    serverBootstrap.channel(NioServerSocketChannel.class);

                    serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            InetSocketAddress remoteAddress = socketChannel.remoteAddress();
                            logger.info("Got a connection from {}:{}", remoteAddress.getHostName(), remoteAddress.getPort());

                            // Add sslCtx if needed
                            if (trustedCertificates != null || certificate != null) {
                                TrustManagerFactory trustManagerFactory = trustedCertificates == null ? null : RSATools.createTrustManagerFactory(trustedCertificates);
                                KeyManagerFactory keyManagerFactory = certificate == null ? null : RSATools.createKeyManagerFactory(certificate);

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

                            // Add the channel handlers
                            for (ChannelHandlerContainer channelHandlerContainer : channelHandlerContainers) {
                                socketChannel.pipeline().addLast(ReflectionTools.instantiate(channelHandlerContainer.getChannelHandlerClass(), channelHandlerContainer.getConstructorParams()));
                            }
                        }
                    }) //
                            .option(ChannelOption.SO_BACKLOG, 128) //
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    bindedPort = port;
                    logger.info("Server on port {} is starting...", port);
                    ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
                    SocketAddress socketAddress = channelFuture.channel().localAddress();
                    if (socketAddress instanceof InetSocketAddress) {
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                        bindedPort = inetSocketAddress.getPort();
                    }
                    logger.info("Server on port {} is started", bindedPort);
                    countDownLatch.countDown();
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    logger.info("Server on port {} is interrupted", bindedPort);
                } finally {
                    countDownLatch.countDown();
                }
                logger.info("Server on port {} is stopped", bindedPort);
            }
        });
        thread.setName("Netty Server-" + bindedPort);
        thread.start();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for the server to start");
        }
    }
}
