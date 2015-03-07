/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * To check some assumptions during execution.
 */
public final class AssertTools {

    /**
     * Check that the value is false.
     * 
     * @param actual
     *            the value
     */
    public static void assertFalse(boolean actual) {
        assertFalse(actual, "Value must be false");
    }

    /**
     * Check that the value is false.
     * 
     * @param actual
     *            the value
     * @param message
     *            the error message to throw
     */
    public static void assertFalse(boolean actual, String message) {
        if (actual) {
            throw new SmallToolsException(message);
        }
    }

    /**
     * Check that the value is not null.
     * 
     * @param actual
     *            the value
     */
    public static void assertNotNull(Object actual) {
        assertNotNull(actual, "Value must not be null");
    }

    /**
     * Check that the value is not null.
     * 
     * @param actual
     *            the value
     * @param message
     *            the error message to throw
     */
    public static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new SmallToolsException(message);
        }
    }

    /**
     * Check that the value is null.
     * 
     * @param actual
     *            the value
     */
    public static void assertNull(Object actual) {
        assertNull(actual, "Value must be null");
    }

    /**
     * Check that the value is null.
     * 
     * @param actual
     *            the value
     * @param message
     *            the error message to throw
     */
    public static void assertNull(Object actual, String message) {
        if (actual != null) {
            throw new SmallToolsException(message);
        }
    }

    /**
     * Check that the value is true.
     * 
     * @param actual
     *            the value
     */
    public static void assertTrue(boolean actual) {
        assertTrue(actual, "Value must be true");
    }

    /**
     * Check that the value is true.
     * 
     * @param actual
     *            the value
     * @param message
     *            the error message to throw
     */
    public static void assertTrue(boolean actual, String message) {
        if (!actual) {
            throw new SmallToolsException(message);
        }
    }

    private AssertTools() {
    }

}
