/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;

/**
 * Retrieve some common environment values.
 */
public class JavaEnvironmentValues {

    /**
     * Get the name of the computer. This is the environment "COMPUTERNAME".
     *
     * @return the name of the computer
     */
    public static String getComputerName() {
        return System.getenv("COMPUTERNAME");
    }

    /**
     * Get the home directory of the current user. This is the system property "user.home".
     *
     * @return the home directory of the current user
     */
    public static String getHomeDirectory() {
        return System.getProperty("user.home");
    }

    /**
     * Get the hostname of this computer.
     *
     * @return the host name of the computer
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return getComputerName();
        }
    }

    /**
     * Get the name of the operating system like "Linux", "Windows Vista", "Windows 7", etc. This is the system property "os.name".
     *
     * @return the name of the operating system
     */
    public static String getOperatingSystem() {
        return System.getProperty("os.name");
    }

    /**
     * Get the temporary directory path. This is the system property "java.io.tmpdir".
     *
     * @return the temporary directory path
     */
    public static String getTemporaryDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Get the name of the current user. This is the system property "user.name".
     *
     * @return the name of the current user
     */
    public static String getUserName() {
        return System.getProperty("user.name");
    }

    /**
     * Get the current working directory. This is the system property "user.dir".
     *
     * @return the current working directory
     */
    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("----[ Environment ]----");
        for (Entry<String, String> entry : System.getenv().entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }

        System.out.println("----[ Properties ]----");
        for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
    }
}
