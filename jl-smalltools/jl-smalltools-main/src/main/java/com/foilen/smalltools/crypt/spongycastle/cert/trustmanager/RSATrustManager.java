/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.spongycastle.cert.trustmanager;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.X509TrustManager;

import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.crypt.spongycastle.cert.RSATrustedCertificates;

public class RSATrustManager implements X509TrustManager {

    private RSATrustedCertificates rsaTrustedCertificates;
    private X509Certificate[] acceptedIssuers;

    public RSATrustManager(RSATrustedCertificates rsaTrustedCertificates) {
        this.rsaTrustedCertificates = rsaTrustedCertificates;
        acceptedIssuers = rsaTrustedCertificates.getTrustedCertificates().stream().map(it -> it.getCertificate()).collect(Collectors.toList()).toArray(new X509Certificate[0]);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkServerTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        // Take all certificates of the chain
        List<RSACertificate> rsaCertificates = new ArrayList<>();
        rsaCertificates.addAll(Arrays.asList(chain).stream().map(it -> new RSACertificate(it)).collect(Collectors.toList()));

        // For each, check trusted with all other as extra intermediates
        for (int i = 0; i < rsaCertificates.size(); ++i) {
            List<RSACertificate> moreIntermediateCertificates = new ArrayList<>(rsaCertificates);
            RSACertificate current = moreIntermediateCertificates.remove(i);
            if (!rsaTrustedCertificates.isTrusted(current, moreIntermediateCertificates)) {
                throw new CertificateException("Certificate " + current.getThumbprint() + " " + current.getCommonName() + " is not trusted");
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return acceptedIssuers;
    }

}
