/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert.trustmanager;

import javax.net.ssl.ManagerFactoryParameters;

import com.foilen.smalltools.crypt.bouncycastle.cert.RSATrustedCertificates;

public class RSAManagerFactoryParameters implements ManagerFactoryParameters {

    private RSATrustedCertificates rsaTrustedCertificates;

    public RSAManagerFactoryParameters(RSATrustedCertificates rsaTrustedCertificates) {
        this.rsaTrustedCertificates = rsaTrustedCertificates;
    }

    public RSATrustedCertificates getRsaTrustedCertificates() {
        return rsaTrustedCertificates;
    }

}
