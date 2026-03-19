package com.foilen.smalltools.crypt.jce.cert.trustmanager;

import com.foilen.smalltools.crypt.jce.cert.RSATrustedCertificates;

import javax.net.ssl.ManagerFactoryParameters;

/**
 * The parameters for the {@link RSATrustManagerFactory}.
 */
public class RSAManagerFactoryParameters implements ManagerFactoryParameters {

    private final RSATrustedCertificates rsaTrustedCertificates;

    /**
     * The trusted certificates.
     *
     * @param rsaTrustedCertificates the trusted certificates
     */
    public RSAManagerFactoryParameters(RSATrustedCertificates rsaTrustedCertificates) {
        this.rsaTrustedCertificates = rsaTrustedCertificates;
    }

    /**
     * Get the trusted certificates.
     *
     * @return the trusted certificates
     */
    public RSATrustedCertificates getRsaTrustedCertificates() {
        return rsaTrustedCertificates;
    }

}
