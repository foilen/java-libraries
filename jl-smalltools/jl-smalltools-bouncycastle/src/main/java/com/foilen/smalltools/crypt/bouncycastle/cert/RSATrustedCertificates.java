/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CloseableTools;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.FileReader;
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

    private Map<X500Name, List<RSACertificate>> trustedCertificatesBySubject = new HashMap<>();
    private Map<X500Name, List<RSACertificate>> intermediateCertificatesBySubject = new HashMap<>();

    /**
     * Add a trusted intermediate certificate.
     *
     * @param certificates the certificates
     * @return this
     */
    public RSATrustedCertificates addIntermediateCertificate(Certificate... certificates) {
        for (Certificate certificate : certificates) {
            addIntermediateRsaCertificate(new RSACertificate(new X509CertificateHolder(certificate)));
        }
        return this;
    }

    /**
     * Add a trusted intermediate certificate.
     *
     * @param certificates the certificates
     * @return this
     */
    public RSATrustedCertificates addIntermediateCertificate(Collection<Certificate> certificates) {
        for (Certificate certificate : certificates) {
            addIntermediateRsaCertificate(new RSACertificate(new X509CertificateHolder(certificate)));
        }
        return this;
    }

    /**
     * Add a trusted intermediate certificate.
     *
     * @param x509CertificateHolders the certificates
     * @return this
     */
    public RSATrustedCertificates addIntermediateCertificateHolder(Collection<X509CertificateHolder> x509CertificateHolders) {
        for (X509CertificateHolder x509CertificateHolder : x509CertificateHolders) {
            addIntermediateRsaCertificate(new RSACertificate(x509CertificateHolder));
        }
        return this;
    }

    /**
     * Add a trusted intermediate certificate.
     *
     * @param x509CertificateHolders the certificates
     * @return this
     */
    public RSATrustedCertificates addIntermediateCertificateHolder(X509CertificateHolder... x509CertificateHolders) {
        for (X509CertificateHolder x509CertificateHolder : x509CertificateHolders) {
            addIntermediateRsaCertificate(new RSACertificate(x509CertificateHolder));
        }
        return this;
    }

    /**
     * Load all the certificates from a pem file.
     *
     * @param filePath the full path to the file
     * @return this
     */
    public RSATrustedCertificates addIntermediateFromPemFile(String filePath) {
        addToList(intermediateCertificatesBySubject, filePath);
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

    private void addToList(Map<X500Name, List<RSACertificate>> certificatesBySubject, RSACertificate rsaCertificate) {

        // Add it by subject
        X500Name subject = rsaCertificate.getCertificateHolder().getSubject();
        List<RSACertificate> rsaCertificates = certificatesBySubject.get(subject);
        if (rsaCertificates == null) {
            rsaCertificates = new ArrayList<>();
            certificatesBySubject.put(subject, rsaCertificates);
        }
        rsaCertificates.add(rsaCertificate);
    }

    private void addToList(Map<X500Name, List<RSACertificate>> certificatesBySubject, String filePath) {

        PemReader reader = null;
        try {
            // Certificate
            reader = new PemReader(new FileReader(filePath));
            PemObject pemObject;
            while ((pemObject = reader.readPemObject()) != null) {
                if ("CERTIFICATE".equals(pemObject.getType())) {
                    RSACertificate rsaCertificate = new RSACertificate();
                    rsaCertificate.setCertificateHolder(new X509CertificateHolder(pemObject.getContent()));

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
     * Add a trusted certificate.
     *
     * @param certificates the certificates
     * @return this
     */
    public RSATrustedCertificates addTrustedCertificate(Certificate... certificates) {
        for (Certificate certificate : certificates) {
            addTrustedRsaCertificate(new RSACertificate(new X509CertificateHolder(certificate)));
        }
        return this;
    }

    /**
     * Add a trusted certificate.
     *
     * @param certificates the certificates
     * @return this
     */
    public RSATrustedCertificates addTrustedCertificate(Collection<Certificate> certificates) {
        for (Certificate certificate : certificates) {
            addTrustedRsaCertificate(new RSACertificate(new X509CertificateHolder(certificate)));
        }
        return this;
    }

    /**
     * Add a trusted certificate.
     *
     * @param x509CertificateHolders the certificates
     * @return this
     */
    public RSATrustedCertificates addTrustedCertificateHolder(Collection<X509CertificateHolder> x509CertificateHolders) {
        for (X509CertificateHolder x509CertificateHolder : x509CertificateHolders) {
            addTrustedRsaCertificate(new RSACertificate(x509CertificateHolder));
        }
        return this;
    }

    /**
     * Add a trusted certificate.
     *
     * @param x509CertificateHolders the certificates
     * @return this
     */
    public RSATrustedCertificates addTrustedCertificateHolder(X509CertificateHolder... x509CertificateHolders) {
        for (X509CertificateHolder x509CertificateHolder : x509CertificateHolders) {
            addTrustedRsaCertificate(new RSACertificate(x509CertificateHolder));
        }
        return this;
    }

    /**
     * Load all the certificates from a pem file.
     *
     * @param filePath the full path to the file
     * @return this
     */
    public RSATrustedCertificates addTrustedFromPemFile(String filePath) {
        addToList(trustedCertificatesBySubject, filePath);
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

        // Empty list
        if (potentialSigners == null) {
            return null;
        }

        // Find in the list
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
        List<RSACertificate> intermediateCertificates = new ArrayList<>();
        for (List<RSACertificate> current : intermediateCertificatesBySubject.values()) {
            intermediateCertificates.addAll(current);
        }
        return intermediateCertificates;
    }

    /**
     * Get the list of trusted certificates.
     *
     * @return the trusted certificates
     */
    public List<RSACertificate> getTrustedCertificates() {
        List<RSACertificate> trustedCertificates = new ArrayList<>();
        for (List<RSACertificate> current : trustedCertificatesBySubject.values()) {
            trustedCertificates.addAll(current);
        }
        return trustedCertificates;
    }

    /**
     * Check that the certificate has a path to any Trusted certificate (using the intermediates certificates if necessary). All the certificates must be in the valid time range.
     *
     * @param rsaCertificate the certificate to validate
     * @return true if is trusted
     */
    public boolean isTrusted(RSACertificate rsaCertificate) {
        Collection<RSACertificate> moreIntermediateCertificates = Collections.emptyList();
        return isTrusted(rsaCertificate, moreIntermediateCertificates);
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

        // Find a signer
        X500Name issuer = rsaCertificate.getCertificateHolder().getIssuer();
        RSACertificate found = findValidSignature(rsaCertificate, trustedCertificatesBySubject.get(issuer));
        if (found != null) {
            // It is trusted signer
            return true;
        }

        found = findValidSignature(rsaCertificate, intermediateCertificatesBySubject.get(issuer));
        if (found != null) {
            // It is an intermediate signer
            return isTrusted(found, moreIntermediateCertificates);
        }

        found = findValidSignature(rsaCertificate, moreIntermediateCertificates);
        if (found != null) {
            // It is an intermediate signer
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
