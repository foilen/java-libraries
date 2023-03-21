/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

/**
 * Tools to manipulate Strings.
 */
public class StringTools {

    /**
     * Compare both Strings, not crashing for nulls..
     *
     * @param first  the first string
     * @param second the second string
     * @return the comparison result
     */
    public static int safeComparisonNullFirst(String first, String second) {
        if (first == null) {
            if (second == null) {
                return 0;
            } else {
                return -1;
            }
        }
        if (second == null) {
            return 1;
        }
        return first.compareTo(second);
    }

    /**
     * Compare both Strings, not crashing for nulls..
     *
     * @param first  the first string
     * @param second the second string
     * @return the comparison result
     */
    public static int safeComparisonNullLast(String first, String second) {
        if (first == null) {
            if (second == null) {
                return 0;
            } else {
                return 1;
            }
        }
        if (second == null) {
            return -1;
        }
        return first.compareTo(second);
    }

    /**
     * Tells if both are equals, not crashing for nulls.
     *
     * @param first  the first string
     * @param second the second string
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
