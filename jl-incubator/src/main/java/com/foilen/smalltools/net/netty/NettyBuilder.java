/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.netty;

import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.crypt.spongycastle.cert.RSATrustedCertificates;

import io.netty.channel.ChannelHandler;

/**
 * To easily create Netty clients and/or servers. Supports TLS (if you set a certificate and/or trusted certificates.
 *
 * <pre>
 * Usage:
 *
 * // Server side
 * NettyBuilder nettyBuilder = new NettyBuilder();
 * nettyBuilder.setCertificate(serverCertificate);
 * nettyBuilder.setTrustedCertificates(clientTrustedCertificates);
 * nettyBuilder.addChannelHandler(CommanderDecoder.class);
 * nettyBuilder.addChannelHandler(CommanderExecutionChannel.class, configureSpring, commanderClient, executorService);
 * nettyBuilder.addChannelHandler(CommanderEncoder.class);
 * nettyServer = nettyBuilder.buildServer(port);
 * port = nettyServer.getPort();
 *
 * // Client side
 * NettyBuilder nettyBuilder = new NettyBuilder();
 * nettyBuilder.setCertificate(clientCertificate);
 * nettyBuilder.setTrustedCertificates(serverTrustedCertificates);
 * nettyBuilder.addChannelHandler(CommanderDecoder.class);
 * nettyBuilder.addChannelHandler(CommanderExecutionChannel.class, configureSpring, this, executorService);
 * nettyBuilder.addChannelHandler(CommanderEncoder.class);
 * NettyClient nettyClient = nettyBuilder.buildClient(host, port);
 * </pre>
 */
public class NettyBuilder {

    private RSATrustedCertificates trustedCertificates;
    private RSACertificate certificate;

    private List<ChannelHandlerContainer> channelHandlerContainers = new ArrayList<>();

    /**
     * Add a channel handler.
     *
     * @param channelHandlerClass
     *            the class type of the channel handler to instantiate
     * @param constructorParams
     *            the parameters of the constructor
     * @return this
     */
    public NettyBuilder addChannelHandler(Class<? extends ChannelHandler> channelHandlerClass, Object... constructorParams) {
        channelHandlerContainers.add(new ChannelHandlerContainer(channelHandlerClass, constructorParams));
        return this;
    }

    public NettyClient buildClient(String hostname, int port) {
        NettyClient nettyClient = new NettyClient();
        nettyClient.connect(hostname, port, trustedCertificates, certificate, channelHandlerContainers);
        return nettyClient;
    }

    /**
     * Build and start the server.
     *
     * @param port
     *            the port to listen on
     * @return the {@link NettyServer}
     */
    public NettyServer buildServer(int port) {
        NettyServer nettyServer = new NettyServer();
        nettyServer.start(port, trustedCertificates, certificate, channelHandlerContainers);
        return nettyServer;
    }

    public RSACertificate getCertificate() {
        return certificate;
    }

    public RSATrustedCertificates getTrustedCertificates() {
        return trustedCertificates;
    }

    /**
     * Set the certificate of this connection.
     *
     * @param certificate
     *            the certificate
     * @return this
     */
    public NettyBuilder setCertificate(RSACertificate certificate) {
        this.certificate = certificate;
        return this;
    }

    /**
     * Set the certificates that we trust to connect to or to receive connections from.
     *
     * @param trustedCertificates
     *            the certificates
     * @return this
     */
    public NettyBuilder setTrustedCertificates(RSATrustedCertificates trustedCertificates) {
        this.trustedCertificates = trustedCertificates;
        return this;
    }

}
