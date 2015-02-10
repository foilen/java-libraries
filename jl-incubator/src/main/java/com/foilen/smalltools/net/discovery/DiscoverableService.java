/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.discovery;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.Assert;
import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.base.Objects;

/**
 * An application's service available on the network. This is to use with the automatic discovery services like {@link LocalBroadcastDiscoveryServer}.
 */
public class DiscoverableService {

    private static final Logger logger = LoggerFactory.getLogger(DiscoverableService.class);

    private static final String SEPARATION_CHAR = "|";

    public static final String TCP = "TCP";
    public static final String UDP = "UDP";

    // To broadcast
    private String appName;
    private String appVersion;
    private String serviceName;
    private String serviceDescription;
    private String serverType; // TCP or UDP
    private int serverPort = -1;

    // Additional details for the client
    private String serverHost;

    public DiscoverableService() {
    }

    public DiscoverableService(byte[] bytes) {
        fromBytes(bytes);
    }

    public DiscoverableService(String appName, String appVersion, String serviceName, String serviceDescription) {
        this.appName = appName;
        this.appVersion = appVersion;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
    }

    /**
     * Remove internally used characters.
     */
    private String cleanupField(String field) {
        if (field == null) {
            return null;
        }
        return field.replaceAll(SEPARATION_CHAR, "");
    }

    /**
     * Remove internally used characters on all fields.
     */
    private void cleanupFields() {
        appName = cleanupField(appName);
        appVersion = cleanupField(appVersion);
        serviceName = cleanupField(serviceName);
        serviceDescription = cleanupField(serviceDescription);
        serverType = cleanupField(serverType);
    }

    /**
     * Connect to the remote service.
     * 
     * @return the connected socket
     */
    public Socket connecToTcpService() {

        Assert.assertNotNull(serverHost, "The server host is not set");
        Assert.assertTrue(serverPort > 0, "The server port is not correct or not set");
        Assert.assertTrue(TCP.equals(serverType), "The server type must be TCP");

        try {
            return new Socket(serverHost, serverPort);
        } catch (Exception e) {
            logger.error("Problem connecting to remote server", e);
            throw new SmallToolsException(e);
        }
    }

    /**
     * Connect to the remote service.
     * 
     * @return the socket ready to send messages
     */
    public DatagramSocket connecToUdpService() {
        Assert.assertNotNull(serverHost, "The server host is not set");
        Assert.assertTrue(serverPort > 0, "The server port is not correct or not set");
        Assert.assertTrue(UDP.equals(serverType), "The server type must be UDP");

        try {
            return new DatagramSocket(serverPort, InetAddress.getByName(serverHost));
        } catch (Exception e) {
            logger.error("Problem connecting to remote server", e);
            throw new SmallToolsException(e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof DiscoverableService)) {
            return false;
        }

        return hashCode() == obj.hashCode();
    }

    public void fromBytes(byte[] bytes) {

        String message = new String(bytes);
        String[] parts = message.split("\\" + SEPARATION_CHAR);

        // Validate
        if (parts.length != 6) {
            throw new SmallToolsException("The message does not contain the right amount of parts");
        }

        // Get parts
        appName = parts[0];
        appVersion = parts[1];
        serviceName = parts[2];
        serviceDescription = parts[3];
        serverType = parts[4];
        try {
            serverPort = Integer.valueOf(parts[5].trim());
        } catch (NumberFormatException e) {
            throw new SmallToolsException("The port number is not a number");
        }
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    /**
     * Server type is TCP or UDP.
     * 
     * @return the server type
     */
    public String getServerType() {
        return serverType;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(appName, appVersion, serviceName, serviceDescription, serverType, serverPort, serverHost);
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Server type is TCP or UDP.
     * 
     * @param serverType
     */
    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public byte[] toBytes() {
        cleanupFields();
        verifyNullValues(appName, appVersion, serviceName, serviceDescription, serverType, serverPort);

        StringBuilder sb = new StringBuilder();

        sb.append(appName).append(SEPARATION_CHAR);
        sb.append(appVersion).append(SEPARATION_CHAR);
        sb.append(serviceName).append(SEPARATION_CHAR);
        sb.append(serviceDescription).append(SEPARATION_CHAR);
        sb.append(serverType).append(SEPARATION_CHAR);
        sb.append(serverPort);

        return sb.toString().getBytes();
    }

    @Override
    public String toString() {
        cleanupFields();

        StringBuilder sb = new StringBuilder();

        sb.append(appName).append(SEPARATION_CHAR);
        sb.append(appVersion).append(SEPARATION_CHAR);
        sb.append(serviceName).append(SEPARATION_CHAR);
        sb.append(serviceDescription).append(SEPARATION_CHAR);
        sb.append(serverType).append(SEPARATION_CHAR);
        sb.append(serverPort).append(SEPARATION_CHAR);
        sb.append(serverHost);

        return sb.toString();
    }

    private void verifyNullValues(Object... objects) {
        for (Object object : objects) {
            if (object == null) {
                throw new SmallToolsException("At least one value is not filled");
            }
        }
    }

}
