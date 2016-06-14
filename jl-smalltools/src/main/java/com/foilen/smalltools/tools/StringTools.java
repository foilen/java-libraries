/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

public class StringTools {

    /**
     * Tells if both are equals, not crashing for nulls.
     * 
     * @param first
     *            the first string
     * @param second
     *            the second string
     * @return true if equals
     */
    public static boolean safeEquals(String first, String second) {

        if (first == null || second == null) {
            return first == null && second == null;
        }

        return first.equals(second);

    }

    private StringTools() {
    }

}
