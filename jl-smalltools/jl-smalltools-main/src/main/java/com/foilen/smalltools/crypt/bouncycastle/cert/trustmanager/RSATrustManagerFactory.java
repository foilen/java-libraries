/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert.trustmanager;

import java.security.InvalidAlgorithmParameterException;

import javax.net.ssl.TrustManagerFactory;

import com.foilen.smalltools.crypt.bouncycastle.cert.RSATrustedCertificates;
import com.foilen.smalltools.exception.SmallToolsException;

public class RSATrustManagerFactory extends TrustManagerFactory {

    public static RSATrustManagerFactory getInstance() {
        return new RSATrustManagerFactory();
    }

    private RSATrustManagerFactory() {
        super(new RSATrustManagerFactorySpi(), null, "BCRSATrustManagerFactory");
    }

    public void init(RSATrustedCertificates rsaTrustedCertificates) {
        try {
            init(new RSAManagerFactoryParameters(rsaTrustedCertificates));
        } catch (InvalidAlgorithmParameterException e) {
            throw new SmallToolsException("Cannot initialize", e);
        }
    }

}
