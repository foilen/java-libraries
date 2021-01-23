/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

public class SystemTools {

    /**
     * Get the property, the environment or null value.
     *
     * @param key
     *            the key
     * @return the value
     */
    public static String getPropertyOrEnvironment(String key) {
        return getPropertyOrEnvironment(key, null);
    }

    /**
     * Get the property, the environment or the default value.
     *
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     * @return the value
     */
    public static String getPropertyOrEnvironment(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }

        value = System.getenv(key);
        if (value != null) {
            return value;
        }

        return defaultValue;
    }

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
