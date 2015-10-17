/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.actions;

import java.io.IOException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.tls.Certificate;
import org.spongycastle.crypto.tls.DefaultTlsClient;
import org.spongycastle.crypto.tls.ServerOnlyTlsAuthentication;
import org.spongycastle.crypto.tls.TlsAuthentication;
import org.spongycastle.crypto.tls.TlsClientProtocol;

import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.streampair.StreamPair;

/**
 * Create a TLS connection to encrypt it. (do not authenticate ; only encrypt)
 * 
 * <pre>
 * Defaults:
 * - negociationTimeoutSeconds = 20
 * </pre>
 * 
 * TODO Add authentication
 * 
 * <pre>
 * Dependencies:
 * compile 'com.madgag.spongycastle:prov:1.51.0.0'
 * compile 'com.madgag.spongycastle:pkix:1.51.0.0'
 * compile 'com.madgag.spongycastle:pg:1.51.0.0'
 * </pre>
 * 
 * Deprecated: Use {@link CommanderClient}
 */
@Deprecated
public class TlsClientConnectionAction extends AbstractTimeoutConnectionAction {

    private final static Logger log = LoggerFactory.getLogger(TlsClientConnectionAction.class);

    public TlsClientConnectionAction() {
        negociationTimeoutSeconds = 20;
    }

    @Override
    protected Connection wrappedExecuteAction(Connection connection) {
        try {

            SecureRandom secureRandom = new SecureRandom();

            // Client
            TlsClientProtocol tlsProtocol = new TlsClientProtocol(connection.getInputStream(), connection.getOutputStream(), secureRandom);
            tlsProtocol.connect(new DefaultTlsClient() {

                @Override
                public TlsAuthentication getAuthentication() throws IOException {
                    return new ServerOnlyTlsAuthentication() {
                        // TODO Change for TlsAuthentication
                        @Override
                        public void notifyServerCertificate(Certificate certificateRequest) throws IOException {
                            // Ignoring the certificate since we do not authenticate
                            // TODO Validate
                        }
                    };
                }

            });

            // Update the pair
            StreamPair streamPair = connection.getStreamPair();
            streamPair.setInputStream(tlsProtocol.getInputStream());
            streamPair.setOutputStream(tlsProtocol.getOutputStream());

        } catch (Exception e) {
            log.error("Problem initializing TLS", e);
            return null;
        }

        return connection;
    }
}
