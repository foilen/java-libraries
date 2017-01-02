/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.cert;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import com.foilen.smalltools.tools.DateTools;

/**
 * Some details for generating a certificate. This is used by {@link RSACertificate}.
 * 
 * <pre>
 * The default values are:
 * commonName: "noname"
 * startDate: now
 * endDate: in one year
 * serial: 1
 * </pre>
 */
public class CertificateDetails {

    private String commonName = "noname";
    private Date startDate = new Date();
    private Date endDate = DateTools.addDate(startDate, Calendar.YEAR, 1);
    private BigInteger serial = BigInteger.ONE;

    public String getCommonName() {
        return commonName;
    }

    public Date getEndDate() {
        return endDate;
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

    public CertificateDetails setSerial(BigInteger serial) {
        this.serial = serial;
        return this;
    }

    public CertificateDetails setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

}
