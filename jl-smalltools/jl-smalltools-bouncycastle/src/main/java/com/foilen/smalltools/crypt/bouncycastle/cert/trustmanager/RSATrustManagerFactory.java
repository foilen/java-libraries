package com.foilen.smalltools.crypt.bouncycastle.cert.trustmanager;

import com.foilen.smalltools.crypt.bouncycastle.cert.RSATrustedCertificates;
import com.foilen.smalltools.exception.SmallToolsException;

import javax.net.ssl.TrustManagerFactory;
import java.security.InvalidAlgorithmParameterException;

/**
 * A factory to create {@link RSATrustManager}.
 */
public class RSATrustManagerFactory extends TrustManagerFactory {

    /**
     * Get an instance of the factory.
     *
     * @return the instance
     */
    public static RSATrustManagerFactory getInstance() {
        return new RSATrustManagerFactory();
    }

    private RSATrustManagerFactory() {
        super(new RSATrustManagerFactorySpi(), null, "BCRSATrustManagerFactory");
    }

    /**
     * Initialize the factory.
     *
     * @param rsaTrustedCertificates the trusted certificates
     */
    public void init(RSATrustedCertificates rsaTrustedCertificates) {
        try {
            init(new RSAManagerFactoryParameters(rsaTrustedCertificates));
        } catch (InvalidAlgorithmParameterException e) {
            throw new SmallToolsException("Cannot initialize", e);
        }
    }

}
