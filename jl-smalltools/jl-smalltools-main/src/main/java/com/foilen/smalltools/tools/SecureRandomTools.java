/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import com.google.common.io.BaseEncoding;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Some methods to get random values.
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

        String text = Base64.getEncoder().encodeToString(bytes);
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

        String text = BaseEncoding.base16().upperCase().encode(bytes);
        return text.substring(0, length);
    }

    private SecureRandomTools() {
    }

    public static void main(String[] args) {
        System.out.println("Base64: " + SecureRandomTools.randomBase64String(20));
        System.out.println("   Hex: " + SecureRandomTools.randomHexString(20));
    }
}
