/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.crypt.bouncycastle.asymmetric;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Contains the details of the RSA key. Given by the method {@link RSACrypt#retrieveKeyDetails(AsymmetricKeys)}.
 */
public class RSAKeyDetails {
    private BigInteger modulus;
    private BigInteger publicExponent;
    private BigInteger privateExponent;

    private boolean isCrt = false;
    private BigInteger primeP;
    private BigInteger primeQ;
    private BigInteger primeExponentP;
    private BigInteger primeExponentQ;
    private BigInteger crtCoefficient;

    /**
     * Create the key details with no values.
     */
    public RSAKeyDetails() {
    }

    /**
     * Create the key details with values.
     *
     * @param modulus         the modulus
     * @param publicExponent  (optional) the public exponent
     * @param privateExponent (optional) the private exponent
     */
    public RSAKeyDetails(BigInteger modulus, BigInteger publicExponent, BigInteger privateExponent) {
        this.modulus = modulus;
        this.publicExponent = publicExponent;
        this.privateExponent = privateExponent;
    }

    /**
     * Create the key details with values.
     *
     * @param modulus         the modulus
     * @param publicExponent  (optional) the public exponent
     * @param privateExponent (optional) the private exponent
     * @param p               (optional) the primeP
     * @param q               (optional) the primeQ
     * @param dP              (optional) the primeExponentP
     * @param dQ              (optional) the primeExponentQ
     * @param qInv            (optional) the crtCoefficient
     */
    public RSAKeyDetails(BigInteger modulus, BigInteger publicExponent, BigInteger privateExponent, BigInteger p, BigInteger q, BigInteger dP, BigInteger dQ, BigInteger qInv) {
        this.modulus = modulus;
        this.publicExponent = publicExponent;
        this.privateExponent = privateExponent;
        this.primeP = p;
        this.primeQ = q;
        this.primeExponentP = dP;
        this.primeExponentQ = dQ;
        this.crtCoefficient = qInv;
        isCrt = true;
    }

    /**
     * Get the crt Coefficient
     *
     * @return the crt Coefficient
     */
    public BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }

    /**
     * Get the JCA private key.
     *
     * @return the JCA private key
     */
    public PrivateKey getJcaPrivateKey() {
        AssertTools.assertNotNull(modulus, "The modulus needs to be set");
        AssertTools.assertNotNull(privateExponent, "The private exponent needs to be set");

        if (isCrt) {
            AssertTools.assertNotNull(primeP, "Since it is CRT, the primeP needs to be set");
            AssertTools.assertNotNull(primeQ, "Since it is CRT, the primeQ needs to be set");
            AssertTools.assertNotNull(primeExponentP, "Since it is CRT, the primeExponentP needs to be set");
            AssertTools.assertNotNull(primeExponentQ, "Since it is CRT, the primeExponentQ needs to be set");
            AssertTools.assertNotNull(crtCoefficient, "Since it is CRT, the crtCoefficient needs to be set");
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (isCrt) {
                KeySpec keySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
                return keyFactory.generatePrivate(keySpec);
            } else {
                KeySpec keySpec = new RSAPrivateKeySpec(modulus, privateExponent);
                return keyFactory.generatePrivate(keySpec);
            }
        } catch (Exception e) {
            throw new SmallToolsException("Could not generate key", e);
        }
    }

    /**
     * Get the JCA public key.
     *
     * @return the JCA public key
     */
    public PublicKey getJcaPublicKey() {
        AssertTools.assertNotNull(modulus, "The modulus needs to be set");
        AssertTools.assertNotNull(publicExponent, "The public exponent needs to be set");
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new SmallToolsException("Could not generate key", e);
        }
    }

    /**
     * @return the modulus
     */
    public BigInteger getModulus() {
        return modulus;
    }

    /**
     * Get the primeExponentP
     *
     * @return the primeExponentP
     */
    public BigInteger getPrimeExponentP() {
        return primeExponentP;
    }

    /**
     * Get the primeExponentQ
     *
     * @return the primeExponentQ
     */
    public BigInteger getPrimeExponentQ() {
        return primeExponentQ;
    }

    /**
     * Get the primeP
     *
     * @return the primeP
     */
    public BigInteger getPrimeP() {
        return primeP;
    }

    /**
     * Get the primeQ
     *
     * @return the primeQ
     */
    public BigInteger getPrimeQ() {
        return primeQ;
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
     * Is this a CRT key?
     *
     * @return true if CRT
     */
    public boolean isCrt() {
        return isCrt;
    }

    /**
     * Set the CRT flag
     *
     * @param isCrt true if CRT
     */
    public void setCrt(boolean isCrt) {
        this.isCrt = isCrt;
    }

    /**
     * Set the crt Coefficient
     *
     * @param crtCoefficient the crt Coefficient
     */
    public void setCrtCoefficient(BigInteger crtCoefficient) {
        this.crtCoefficient = crtCoefficient;
    }

    /**
     * Set the modulus
     *
     * @param modulus the modulus to set
     */
    public void setModulus(BigInteger modulus) {
        this.modulus = modulus;
    }

    /**
     * Set the primeExponentP
     *
     * @param primeExponentP the primeExponentP to set
     */
    public void setPrimeExponentP(BigInteger primeExponentP) {
        this.primeExponentP = primeExponentP;
    }

    /**
     * Set the primeExponentQ
     *
     * @param primeExponentQ the primeExponentQ to set
     */
    public void setPrimeExponentQ(BigInteger primeExponentQ) {
        this.primeExponentQ = primeExponentQ;
    }

    /**
     * Set the primeP
     *
     * @param primeP the primeP to set
     */
    public void setPrimeP(BigInteger primeP) {
        this.primeP = primeP;
    }

    /**
     * Set the primeQ
     *
     * @param primeQ the primeQ to set
     */
    public void setPrimeQ(BigInteger primeQ) {
        this.primeQ = primeQ;
    }

    /**
     * Set the privateExponent
     *
     * @param privateExponent the privateExponent to set
     */
    public void setPrivateExponent(BigInteger privateExponent) {
        this.privateExponent = privateExponent;
    }

    /**
     * Set the publicExponent
     *
     * @param publicExponent the publicExponent to set
     */
    public void setPublicExponent(BigInteger publicExponent) {
        this.publicExponent = publicExponent;
    }

}
