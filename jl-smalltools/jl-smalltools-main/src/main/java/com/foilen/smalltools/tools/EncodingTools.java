/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

/**
 * Some methods to encode in different formats.
 */
public final class EncodingTools {

    /**
     * Encode the bytes in hex representation.
     *
     * @param bytes
     *            the bytes to encode
     * @return the hex
     */
    public static String toHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    private EncodingTools() {
    }
}
