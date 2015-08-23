/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.crypt.asymmetric;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;

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
     * Get the JCA private key.
     * 
     * @return the JCA private key
     */
    public PrivateKey getJcaPrivateKey() {
        AssertTools.assertNotNull(modulus, "The modulus needs to be set");
        AssertTools.assertNotNull(privateExponent, "The private exponent needs to be set");
        try {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        KeySpec keySpec = new RSAPrivateKeySpec(modulus, privateExponent);
        return keyFactory.generatePrivate(keySpec);
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
