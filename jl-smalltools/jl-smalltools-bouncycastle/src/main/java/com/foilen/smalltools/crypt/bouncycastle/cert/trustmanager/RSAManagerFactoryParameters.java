/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert.trustmanager;

import com.foilen.smalltools.crypt.bouncycastle.cert.RSATrustedCertificates;

import javax.net.ssl.ManagerFactoryParameters;

/**
 * The parameters for the {@link RSATrustManagerFactory}.
 */
public class RSAManagerFactoryParameters implements ManagerFactoryParameters {

    private RSATrustedCertificates rsaTrustedCertificates;

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
