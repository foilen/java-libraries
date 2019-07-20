/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

/**
 * Some methods to get random values.
 * 
 * <pre>
 * Dependencies:
 * compile "javax.xml.bind:jaxb-api:2.3.1"
 * </pre>
 */
public class SecureRandomTools {

    public static String randomBase64String(int length) {

        AssertTools.assertTrue(length > 0, "The length must be at least 1");

        int bytesLength = length * 3 / 4;
        if (length % 4 == 1) {
            ++bytesLength;
        }

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[bytesLength];
        random.nextBytes(bytes);

        String text = DatatypeConverter.printBase64Binary(bytes);
        return text.substring(0, length);
    }

    public static String randomHexString(int length) {

        AssertTools.assertTrue(length > 0, "The length must be at least 1");

        int bytesLength = length / 2;
        if (length % 2 == 1) {
            ++bytesLength;
        }

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[bytesLength];
        random.nextBytes(bytes);

        String text = DatatypeConverter.printHexBinary(bytes);
        return text.substring(0, length);
    }

    private SecureRandomTools() {
    }
}
