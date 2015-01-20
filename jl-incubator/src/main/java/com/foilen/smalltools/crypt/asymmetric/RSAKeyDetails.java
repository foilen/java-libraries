/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import java.math.BigInteger;

/**
 * Contains the details of the RSA key. Given by the method {@link RSACrypt#retrieveKeyDetails(AsymmetricKeys)}.
 */
public class RSAKeyDetails {
    private BigInteger modulus;
    private BigInteger publicExponent;
    private BigInteger privateExponent;

    public RSAKeyDetails() {
    }

    /**
     * Create the key details with values.
     * 
     * @param modulus
     *            the modulus
     * @param publicExponent
     *            (optional) the public exponent
     * @param privateExponent
     *            (optional) the private exponent
     */
    public RSAKeyDetails(BigInteger modulus, BigInteger publicExponent, BigInteger privateExponent) {
        this.modulus = modulus;
        this.publicExponent = publicExponent;
        this.privateExponent = privateExponent;
    }

    /**
     * @return the modulus
     */
    public BigInteger getModulus() {
        return modulus;
    }

    /**
     * @return the privateExponent
     */
    public BigInteger getPrivateExponent() {
        return privateExponent;
    }

    /**
     * @return the publicExponent
     */
    public BigInteger getPublicExponent() {
        return publicExponent;
    }

    /**
     * @param modulus
     *            the modulus to set
     */
    public void setModulus(BigInteger modulus) {
        this.modulus = modulus;
    }

    /**
     * @param privateExponent
     *            the privateExponent to set
     */
    public void setPrivateExponent(BigInteger privateExponent) {
        this.privateExponent = privateExponent;
    }

    /**
     * @param publicExponent
     *            the publicExponent to set
     */
    public void setPublicExponent(BigInteger publicExponent) {
        this.publicExponent = publicExponent;
    }

}
