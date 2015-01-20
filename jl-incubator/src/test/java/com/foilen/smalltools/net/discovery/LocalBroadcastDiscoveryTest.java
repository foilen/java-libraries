/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.discovery;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.foilen.smalltools.net.discovery.DiscoverableService;
import com.foilen.smalltools.net.discovery.LocalBroadcastDiscoveryClient;
import com.foilen.smalltools.net.discovery.LocalBroadcastDiscoveryServer;
import com.foilen.smalltools.net.services.SocketCallback;

/**
 * Tests for {@link LocalBroadcastDiscoveryServer} and {@link LocalBroadcastDiscoveryClient}.
 */
public class LocalBroadcastDiscoveryTest implements SocketCallback {

    private static final int PORT = 22299;
    private static final String appName = "junit";
    private static final String appVersion = "1.0";
    private static final String serviceName = "service name";
    private static final String serviceDescription = "description";

    private AtomicBoolean gotConnection;

    @Before
    public void setUp() {
        gotConnection = new AtomicBoolean();
    }

    @Test(timeout = 30000)
    public void testDiscovery() throws InterruptedException {

        // Create a service
        LocalBroadcastDiscoveryServer discoveryServer = new LocalBroadcastDiscoveryServer(PORT, 1000);
        DiscoverableService discoverableService = new DiscoverableService(appName, appVersion, serviceName, serviceDescription);
        discoveryServer.addTcpBroadcastService(discoverableService, this);
        Assert.assertTrue(discoverableService.toString().startsWith("junit|1.0|service name|description|TCP|"));

        // Try to retrieve the service
        LocalBroadcastDiscoveryClient discoveryClient = new LocalBroadcastDiscoveryClient(PORT);
        List<DiscoverableService> servicesList = discoveryClient.retrieveServicesList();
        while (servicesList.isEmpty()) {
            Thread.sleep(100);
            servicesList = discoveryClient.retrieveServicesList();
        }

        // Check this is the right service
        Assert.assertEquals(1, servicesList.size());
        DiscoverableService discoverableServiceRetrieved = servicesList.get(0);
        Assert.assertEquals(appName, discoverableServiceRetrieved.getAppName());
        Assert.assertEquals(appVersion, discoverableServiceRetrieved.getAppVersion());
        Assert.assertEquals(serviceName, discoverableServiceRetrieved.getServiceName());
        Assert.assertEquals(serviceDescription, discoverableServiceRetrieved.getServiceDescription());
        Assert.assertEquals(DiscoverableService.TCP, discoverableServiceRetrieved.getServerType());
        Assert.assertTrue(discoverableServiceRetrieved.getServerPort() > 0);

        // Try to connect
        discoverableServiceRetrieved.connecToTcpService();

        while (!gotConnection.get()) {
        }
    }

    @Override
    public void newClient(Socket socket) {
        gotConnection.set(true);
    }

}
