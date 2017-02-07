/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

public class SystemTools {

    /**
     * Sets a system property if not already defined.
     *
     * @param key
     *            the property's key
     * @param value
     *            the property's value
     */
    public static void setPropertyIfNotSet(String key, String value) {
        String currentValue = System.getProperty(key);
        if (currentValue == null) {
            System.setProperty(key, value);
        }
    }

    private SystemTools() {
    }

}
