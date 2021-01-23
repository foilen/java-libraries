/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.foilen.smalltools.tools.DateTools;

/**
 * Some details for generating a certificate. This is used by {@link RSACertificate}.
 *
 * <pre>
 * The default values are:
 * commonName: "noname"
 * sanDns: empty
 * startDate: now
 * endDate: in one year
 * serial: 1
 * </pre>
 */
public class CertificateDetails {

    private String commonName = "noname";
    private List<String> sanDns = new ArrayList<>();
    private Date startDate = new Date();
    private Date endDate = DateTools.addDate(startDate, Calendar.YEAR, 1);
    private BigInteger serial = BigInteger.ONE;

    /**
     * Add some Subject Alternative Names that are DNS names.
     *
     * @param sanDns
     *            the list
     * @return this
     */
    public CertificateDetails addSanDns(String... sanDns) {
        for (String san : sanDns) {
            this.sanDns.add(san);
        }
        return this;
    }

    public String getCommonName() {
        return commonName;
    }

    public Date getEndDate() {
        return endDate;
    }

    public List<String> getSanDns() {
        return sanDns;
    }

    public BigInteger getSerial() {
        return serial;
    }

    public Date getStartDate() {
        return startDate;
    }

    public CertificateDetails setCommonName(String commonName) {
        this.commonName = commonName;
        return this;
    }

    public CertificateDetails setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Set some Subject Alternative Names that are DNS names.
     *
     * @param sanDns
     *            the list
     * @return this
     */
    public CertificateDetails setSanDns(List<String> sanDns) {
        this.sanDns = sanDns;
        return this;
    }

    public CertificateDetails setSerial(BigInteger serial) {
        this.serial = serial;
        return this;
    }

    public CertificateDetails setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

}
