package com.foilen.smalltools.crypt.bouncycastle.cert.trustmanager;

import com.foilen.smalltools.crypt.bouncycastle.cert.RSACertificate;
import com.foilen.smalltools.crypt.bouncycastle.cert.RSATrustedCertificates;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A trust manager that will check if the certificate is trusted.
 */
public class RSATrustManager implements X509TrustManager {

    private RSATrustedCertificates rsaTrustedCertificates;
    private X509Certificate[] acceptedIssuers;

    /**
     * The trusted certificates.
     *
     * @param rsaTrustedCertificates the trusted certificates
     */
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
