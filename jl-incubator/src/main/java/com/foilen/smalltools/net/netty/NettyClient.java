/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.netty;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.crypt.spongycastle.cert.RSATools;
import com.foilen.smalltools.crypt.spongycastle.cert.RSATrustedCertificates;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.AssertTools;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.CipherSuiteFilter;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;

/**
 * A client created with {@link NettyBuilder}.
 */
public class NettyClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    protected Channel channel;

    public NettyClient() {
    }

    public NettyClient(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void close() {
        if (channel != null) {
            logger.info("Closing client {}", channel.remoteAddress());
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
            }
        }
        channel = null;
    }

    public void connect(String hostname, int port, final RSATrustedCertificates trustedCertificates, final RSACertificate certificate, final List<ChannelHandlerContainer> channelHandlerContainers) {

        AssertTools.assertNull(channel, "Client is already connected");

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(NettyCommon.EVENT_LOOP_GROUP);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {

                    // Add sslCtx if needed
                    if (trustedCertificates != null || certificate != null) {
                        TrustManagerFactory trustManagerFactory = trustedCertificates == null ? null : RSATools.createTrustManagerFactory(trustedCertificates);
                        KeyManagerFactory keyManagerFactory = certificate == null ? null : RSATools.createKeyManagerFactory(certificate);

                        CipherSuiteFilter cipherFilter = IdentityCipherSuiteFilter.INSTANCE;
                        SslContext sslCtx = SslContext.newClientContext(SslProvider.JDK, null, trustManagerFactory, null, null, null, keyManagerFactory, null, cipherFilter, null, 0, 0);
                        socketChannel.pipeline().addLast(sslCtx.newHandler(socketChannel.alloc()));
                    }

                    // Add the channel handlers
                    for (ChannelHandlerContainer channelHandlerContainer : channelHandlerContainers) {
                        socketChannel.pipeline().addLast(ReflectionTools.instantiate(channelHandlerContainer.getChannelHandlerClass(), channelHandlerContainer.getConstructorParams()));
                    }

                }
            });

            logger.info("Connecting to {}:{}", hostname, port);
            channel = bootstrap.connect(hostname, port).sync().channel();
        } catch (InterruptedException e) {
            logger.info("Connection to {}:{} was interrupted while being created", hostname, port);
            throw new SmallToolsException("Connection was interrupted");
        }

    }

    /**
     * Get the ip of the remote connection when connected.
     *
     * @return the ip or null if not available
     */
    public String getPeerIp() {
        try {
            return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        } catch (Exception e) {
            logger.error("Requesting the peer's IP, but it is not available", e);
        }

        return null;
    }

    /**
     * Get the SSL certificate if there is a connection using SSL and that the handshake is completed. (This side needs to trust the other side and the other side needs to have a certificate)
     *
     * @return the certificate or null if it is not ready or available
     */
    public List<RSACertificate> getPeerSslCertificate() {
        try {
            X509Certificate[] certificates = channel.pipeline().get(SslHandler.class).engine().getSession().getPeerCertificateChain();

            List<RSACertificate> rsaCertificates = new ArrayList<>();
            for (X509Certificate certificate : certificates) {
                rsaCertificates.add(new RSACertificate(certificate));
            }
            return rsaCertificates;
        } catch (Exception e) {
            logger.error("Requesting the peer's certificate, but it is not present", e);
        }

        return null;
    }

    /**
     * Get the remote address where it is connected.
     *
     * @return the remote address
     */
    public SocketAddress getRemoteAddress() {
        AssertTools.assertNotNull(channel, "Client is not connected");
        return channel.remoteAddress();
    }

    /**
     * Tells if the client is currently connected.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        if (channel == null) {
            return false;
        } else {
            return channel.isOpen();
        }
    }

    /**
     * Send a message and flush.
     *
     * @param msg
     *            the message
     */
    public void writeFlush(Object msg) {
        AssertTools.assertNotNull(channel, "Client is not connected");
        channel.writeAndFlush(msg);
    }

    /**
     * Send a message, flush and wait for it to be sent.
     *
     * @param msg
     *            the message
     */
    public void writeFlushWait(Object msg) {
        AssertTools.assertNotNull(channel, "Client is not connected");
        try {
            channel.writeAndFlush(msg).sync();
        } catch (InterruptedException e) {
            throw new SmallToolsException("Sending data was interrupted");
        }
    }
}
