package com.foilen.smalltools.crypt.jce.cert;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CloseableTools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * List multiple certificates from different sources that you trust and use the validation to check a certificate. All the certificates must be valid in time.
 *
 * <pre>
 * RSACrypt rsaCrypt = new RSACrypt();
 * RSACertificate certA = new RSACertificate(rsaCrypt.generateKeyPair(4096)).selfSign(new CertificateDetails().setCommonName(&quot;A&quot;));
 * RSACertificate certAB = certA.signPublicKey(rsaCrypt.generateKeyPair(4096), new CertificateDetails().setCommonName(&quot;AB&quot;));
 * RSACertificate certABC = certAB.signPublicKey(rsaCrypt.generateKeyPair(4096), new CertificateDetails().setCommonName(&quot;ABC&quot;));
 *
 * RSATrustedCertificates rsaTrustedCertificates = new RSATrustedCertificates();
 * rsaTrustedCertificates.addTrustedRsaCertificate(certA);
 * rsaTrustedCertificates.addIntermediateRsaCertificate(certAB);
 *
 * rsaTrustedCertificates.isTrusted(certA); // True
 * rsaTrustedCertificates.isTrusted(certAB); // True
 * rsaTrustedCertificates.isTrusted(certABC); // True
 * </pre>
 */
public class RSATrustedCertificates {

    // Key: subject DN (RFC 2253 string from X500Principal.getName())
    private final Map<String, List<RSACertificate>> trustedCertificatesBySubject = new HashMap<>();
    private final Map<String, List<RSACertificate>> intermediateCertificatesBySubject = new HashMap<>();

    /**
     * Load all the certificates from a pem file.
     *
     * @param filePath the full path to the file
     * @return this
     */
    public RSATrustedCertificates addIntermediateFromPemFile(String filePath) {
        addToListFromFile(intermediateCertificatesBySubject, filePath);
        return this;
    }

    /**
     * Add a trusted intermediate certificate.
     *
     * @param rsaCertificates the certificates
     * @return this
     */
    public RSATrustedCertificates addIntermediateRsaCertificate(Collection<RSACertificate> rsaCertificates) {
        for (RSACertificate rsaCertificate : rsaCertificates) {
            addToList(intermediateCertificatesBySubject, rsaCertificate);
        }
        return this;
    }

    /**
     * Add a trusted intermediate certificate.
     *
     * @param rsaCertificates the certificates
     * @return this
     */
    public RSATrustedCertificates addIntermediateRsaCertificate(RSACertificate... rsaCertificates) {
        for (RSACertificate rsaCertificate : rsaCertificates) {
            addToList(intermediateCertificatesBySubject, rsaCertificate);
        }
        return this;
    }

    private void addToList(Map<String, List<RSACertificate>> certificatesBySubject, RSACertificate rsaCertificate) {
        String subject = rsaCertificate.getSubjectDn();
        List<RSACertificate> list = certificatesBySubject.get(subject);
        if (list == null) {
            list = new ArrayList<>();
            certificatesBySubject.put(subject, list);
        }
        list.add(rsaCertificate);
    }

    private void addToListFromFile(Map<String, List<RSACertificate>> certificatesBySubject, String filePath) {
        BufferedReader reader = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("-----BEGIN CERTIFICATE-----")) {
                    StringBuilder sb = new StringBuilder();
                    String certLine;
                    while ((certLine = reader.readLine()) != null) {
                        certLine = certLine.trim();
                        if (certLine.equals("-----END CERTIFICATE-----")) {
                            break;
                        }
                        sb.append(certLine).append('\n');
                    }
                    byte[] der = Base64.getMimeDecoder().decode(sb.toString());
                    X509Certificate x509 = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));
                    RSACertificate rsaCertificate = new RSACertificate(x509);
                    addToList(certificatesBySubject, rsaCertificate);
                }
            }
        } catch (Exception e) {
            throw new SmallToolsException("Problem loading the certificates", e);
        } finally {
            CloseableTools.close(reader);
        }
    }

    /**
     * Load all the certificates from a pem file.
     *
     * @param filePath the full path to the file
     * @return this
     */
    public RSATrustedCertificates addTrustedFromPemFile(String filePath) {
        addToListFromFile(trustedCertificatesBySubject, filePath);
        return this;
    }

    /**
     * Add a trusted certificate.
     *
     * @param rsaCertificates the certificates
     * @return this
     */
    public RSATrustedCertificates addTrustedRsaCertificate(Collection<RSACertificate> rsaCertificates) {
        for (RSACertificate rsaCertificate : rsaCertificates) {
            addToList(trustedCertificatesBySubject, rsaCertificate);
        }
        return this;
    }

    /**
     * Add a trusted certificate.
     *
     * @param rsaCertificates the certificates
     * @return this
     */
    public RSATrustedCertificates addTrustedRsaCertificate(RSACertificate... rsaCertificates) {
        for (RSACertificate rsaCertificate : rsaCertificates) {
            addToList(trustedCertificatesBySubject, rsaCertificate);
        }
        return this;
    }

    /**
     * Find the certificate that signed the certificate.
     *
     * @param signedCertificate the certificate that should be signed by it
     * @param potentialSigners  the certificates that might be the signers
     * @return the certificate that signed or null if none
     */
    private RSACertificate findValidSignature(RSACertificate signedCertificate, Collection<RSACertificate> potentialSigners) {
        if (potentialSigners == null) {
            return null;
        }
        for (RSACertificate potentialSigner : potentialSigners) {
            if (signedCertificate.isValidSignature(potentialSigner) && potentialSigner.isValidDate()) {
                return potentialSigner;
            }
        }
        return null;
    }

    /**
     * Get the list of intermediates certificates.
     *
     * @return the intermediates certificates
     */
    public List<RSACertificate> getIntermediatesCertificates() {
        List<RSACertificate> result = new ArrayList<>();
        for (List<RSACertificate> current : intermediateCertificatesBySubject.values()) {
            result.addAll(current);
        }
        return result;
    }

    /**
     * Get the list of trusted certificates.
     *
     * @return the trusted certificates
     */
    public List<RSACertificate> getTrustedCertificates() {
        List<RSACertificate> result = new ArrayList<>();
        for (List<RSACertificate> current : trustedCertificatesBySubject.values()) {
            result.addAll(current);
        }
        return result;
    }

    /**
     * Check that the certificate has a path to any Trusted certificate (using the intermediates certificates if necessary). All the certificates must be in the valid time range.
     *
     * @param rsaCertificate the certificate to validate
     * @return true if is trusted
     */
    public boolean isTrusted(RSACertificate rsaCertificate) {
        return isTrusted(rsaCertificate, Collections.<RSACertificate>emptyList());
    }

    /**
     * Check that the certificate has a path to any Trusted certificate (using the intermediates certificates if necessary). All the certificates must be in the valid time range.
     *
     * @param rsaCertificate               the certificate to validate
     * @param moreIntermediateCertificates more intermediate certificates to use only to validate this one
     * @return true if is trusted
     */
    public boolean isTrusted(RSACertificate rsaCertificate, Collection<RSACertificate> moreIntermediateCertificates) {

        // Validate date
        if (!rsaCertificate.isValidDate()) {
            return false;
        }

        // Look up by issuer DN
        String issuerDn = rsaCertificate.getIssuerDn();

        RSACertificate found = findValidSignature(rsaCertificate, trustedCertificatesBySubject.get(issuerDn));
        if (found != null) {
            return true;
        }

        found = findValidSignature(rsaCertificate, intermediateCertificatesBySubject.get(issuerDn));
        if (found != null) {
            return isTrusted(found, moreIntermediateCertificates);
        }

        found = findValidSignature(rsaCertificate, moreIntermediateCertificates);
        if (found != null) {
            return isTrusted(found, moreIntermediateCertificates);
        }

        return false;
    }

    /**
     * Check that the certificate has a path to any Trusted certificate (using the intermediates certificates if necessary). All the certificates must be in the valid time range.
     *
     * @param certificate                  the certificate to validate
     * @param moreIntermediateCertificates more intermediate certificates to use only to validate this one
     * @return true if is trusted
     */
    public boolean isTrusted(RSACertificate certificate, RSACertificate... moreIntermediateCertificates) {
        return isTrusted(certificate, Arrays.asList(moreIntermediateCertificates));
    }
}
