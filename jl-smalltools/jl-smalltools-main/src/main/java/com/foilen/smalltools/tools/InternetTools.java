/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternetTools {

    private final static Logger logger = LoggerFactory.getLogger(InternetTools.class);

    private static final String LOOPBACK_START_IP_V4 = "127.";
    private static final String LOOPBACK_START_IP_V6_1 = "fe80:";
    private static final String LOOPBACK_START_IP_V6_2 = "0:0:0:0:0:0:0:1";

    /**
     * List all the ips of all the interfaces, but the local loops.
     *
     * @return the ips
     */
    public static Set<String> getAllInterfacesIps() {
        Set<String> ips = new HashSet<>();
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String address = inetAddress.getHostAddress();

                    // Check for non loopback
                    if (isIpLocalLoop(address)) {
                        continue;
                    }

                    // Check for ipv6
                    if (address.contains(":")) {
                        int percentPos = address.lastIndexOf("%");
                        if (percentPos != -1) {
                            address = address.substring(0, percentPos);
                        }
                    }
                    ips.add(address);

                }
            }
        } catch (SocketException e) {
            logger.error("Could not get the network interfaces", e);
        }
        return ips;
    }

    /**
     * Get the public ip of this machine by asking a remote server.
     *
     * @return the public ip or null if there is a problem connecting to the remote server
     */
    public static String getPublicIp() {
        try {
            return StreamsTools.consumeAsString(new URL("https://checkip.foilen.com/").openStream());
        } catch (Exception e) {
            logger.error("Could not retrieve the public ip", e);
            return null;
        }
    }

    /**
     * Tells if the ip is a 127.X.X.X, fe80:X:X:X:X:X:X:X or 0:0:0:0:0:0:0:1 address.
     *
     * @param ip
     *            the ip to check
     * @return true if is local
     */
    public static boolean isIpLocalLoop(String ip) {
        return ip.startsWith(LOOPBACK_START_IP_V4) || ip.toLowerCase().startsWith(LOOPBACK_START_IP_V6_1) || ip.equals(LOOPBACK_START_IP_V6_2);
    }

    public static void main(String[] args) {
        System.out.println("Public Internet IP");
        System.out.println("\t[" + getPublicIp() + "]");
        System.out.println();
        System.out.println("All IPS");
        for (String ip : getAllInterfacesIps()) {
            System.out.println("\t[" + ip + "]");
        }
    }

}
