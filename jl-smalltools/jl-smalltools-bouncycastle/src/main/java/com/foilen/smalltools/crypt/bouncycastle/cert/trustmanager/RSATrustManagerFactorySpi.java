package com.foilen.smalltools.crypt.bouncycastle.cert.trustmanager;

import com.foilen.smalltools.crypt.bouncycastle.cert.RSATrustedCertificates;
import com.foilen.smalltools.exception.SmallToolsException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import java.security.KeyStore;

/**
 * The factory.
 */
public class RSATrustManagerFactorySpi extends TrustManagerFactorySpi {

    private TrustManager[] trustManagers;

    /**
     * Get the trust managers.
     *
     * @return the trust managers
     */
    @Override
    protected TrustManager[] engineGetTrustManagers() {
        return trustManagers;
    }

    /**
     * Init the engine.
     *
     * @param ks the key store or null
     */
    @Override
    protected void engineInit(KeyStore ks) {
        throw new SmallToolsException("Cannot use like that");
    }

    /**
     * Init the engine.
     *
     * @param spec the parameters
     */
    @Override
    protected void engineInit(ManagerFactoryParameters spec) {
        engineInit(((RSAManagerFactoryParameters) spec).getRsaTrustedCertificates());
    }

    /**
     * Init the engine.
     *
     * @param rsaTrustedCertificates the certificates
     */
    protected void engineInit(RSATrustedCertificates rsaTrustedCertificates) {
        trustManagers = new TrustManager[]{new RSATrustManager(rsaTrustedCertificates)};
    }

}
