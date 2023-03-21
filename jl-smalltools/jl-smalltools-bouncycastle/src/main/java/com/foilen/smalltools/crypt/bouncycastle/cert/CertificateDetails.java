/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.cert;

import com.foilen.smalltools.tools.DateTools;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
     * @param sanDns the list
     * @return this
     */
    public CertificateDetails addSanDns(String... sanDns) {
        for (String san : sanDns) {
            this.sanDns.add(san);
        }
        return this;
    }

    /**
     * Get the common name.
     *
     * @return the common name
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * Get the end date.
     *
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Get the Subject Alternative Names that are DNS names.
     *
     * @return the list
     */
    public List<String> getSanDns() {
        return sanDns;
    }

    /**
     * Get the serial.
     *
     * @return the serial
     */
    public BigInteger getSerial() {
        return serial;
    }

    /**
     * Get the start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set the common name.
     *
     * @param commonName the common name
     * @return this
     */
    public CertificateDetails setCommonName(String commonName) {
        this.commonName = commonName;
        return this;
    }

    /**
     * Set the end date.
     *
     * @param endDate the end date
     * @return this
     */
    public CertificateDetails setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Set some Subject Alternative Names that are DNS names.
     *
     * @param sanDns the list
     * @return this
     */
    public CertificateDetails setSanDns(List<String> sanDns) {
        this.sanDns = sanDns;
        return this;
    }

    /**
     * Set the serial.
     *
     * @param serial the serial
     * @return this
     */
    public CertificateDetails setSerial(BigInteger serial) {
        this.serial = serial;
        return this;
    }

    /**
     * Set the start date.
     *
     * @param startDate the start date
     * @return this
     */
    public CertificateDetails setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

}
