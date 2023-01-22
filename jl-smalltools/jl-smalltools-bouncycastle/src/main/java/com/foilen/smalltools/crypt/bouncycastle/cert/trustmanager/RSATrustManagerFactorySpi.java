/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert.trustmanager;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

import com.foilen.smalltools.crypt.bouncycastle.cert.RSATrustedCertificates;
import com.foilen.smalltools.exception.SmallToolsException;

public class RSATrustManagerFactorySpi extends TrustManagerFactorySpi {

    private TrustManager[] trustManagers;

    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return trustManagers;
    }

    @Override
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        throw new SmallToolsException("Cannot use like that");
    }

    @Override
    protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        engineInit(((RSAManagerFactoryParameters) spec).getRsaTrustedCertificates());
    }

    protected void engineInit(RSATrustedCertificates rsaTrustedCertificates) {
        trustManagers = new TrustManager[] { new RSATrustManager(rsaTrustedCertificates) };
    }

}
