/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.actions;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v1CertificateBuilder;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.KeyGenerationParameters;
import org.spongycastle.crypto.generators.RSAKeyPairGenerator;
import org.spongycastle.crypto.params.RSAKeyGenerationParameters;
import org.spongycastle.crypto.tls.Certificate;
import org.spongycastle.crypto.tls.DefaultTlsServer;
import org.spongycastle.crypto.tls.DefaultTlsSignerCredentials;
import org.spongycastle.crypto.tls.TlsServerProtocol;
import org.spongycastle.crypto.tls.TlsSignerCredentials;
import org.spongycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.spongycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.spongycastle.operator.bc.BcRSAContentSignerBuilder;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.streampair.StreamPair;

/**
 * Create a TLS connection to encrypt it. (do not authenticate ; only encrypt)
 * 
 * <pre>
 * Defaults:
 * - rsaKeySize = 2048
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
 */
public class TlsServerConnectionAction extends AbstractTimeoutConnectionAction {

    private final static Logger log = LoggerFactory.getLogger(TlsServerConnectionAction.class);

    private int rsaKeySize = 2048;

    private AsymmetricCipherKeyPair rsaKeyPair;
    private Certificate certificate;

    public TlsServerConnectionAction() {
        try {
            negociationTimeoutSeconds = 20;

            // Init the certificate for the server

            // Generate the key
            SecureRandom secureRandom = new SecureRandom();
            RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
            KeyGenerationParameters param = new RSAKeyGenerationParameters(new BigInteger("65537"), secureRandom, rsaKeySize, 80);
            rsaKeyPairGenerator.init(param);
            rsaKeyPair = rsaKeyPairGenerator.generateKeyPair();

            // Generate the self-signed certificate
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

            ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(rsaKeyPair.getPrivate());
            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(rsaKeyPair.getPublic());

            Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
            Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);

            X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(new X500Name("CN=self"), BigInteger.ONE, startDate, endDate, new X500Name("CN=self"), subPubKeyInfo);

            X509CertificateHolder certHolder = v1CertGen.build(sigGen);
            certificate = new Certificate(new org.spongycastle.asn1.x509.Certificate[] { certHolder.toASN1Structure() });
        } catch (Exception e) {
            throw new SmallToolsException("Problem generating the certificate", e);
        }
    }

    public int getRsaKeySize() {
        return rsaKeySize;
    }

    public void setRsaKeySize(int rsaKeySize) {
        this.rsaKeySize = rsaKeySize;
    }

    @Override
    protected Connection wrappedExecuteAction(Connection connection) {
        try {

            SecureRandom secureRandom = new SecureRandom();
            // Server
            TlsServerProtocol tlsProtocol = new TlsServerProtocol(connection.getInputStream(), connection.getOutputStream(), secureRandom);
            tlsProtocol.accept(new DefaultTlsServer() {

                @Override
                protected TlsSignerCredentials getRSASignerCredentials() throws IOException {
                    return new DefaultTlsSignerCredentials(context, certificate, rsaKeyPair.getPrivate());
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
