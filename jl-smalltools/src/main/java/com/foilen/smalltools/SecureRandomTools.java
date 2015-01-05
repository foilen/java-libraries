/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import java.security.SecureRandom;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 * Some methods to get random values.
 */
public class SecureRandomTools {

    public static String randomBase64String(int length) {

        Assert.assertTrue(length > 0, "The length must be at least 1");

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);

        String text = String.valueOf(Base64Coder.encode(bytes));
        return text.substring(0, length);
    }

    private SecureRandomTools() {
    }
}
