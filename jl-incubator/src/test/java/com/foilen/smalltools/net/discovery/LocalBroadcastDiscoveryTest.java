/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

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

import com.foilen.smalltools.net.services.SocketCallback;
import com.foilen.smalltools.tools.ThreadTools;

/**
 * Tests for {@link LocalBroadcastDiscoveryServer} and {@link LocalBroadcastDiscoveryClient}.
 */
public class LocalBroadcastDiscoveryTest implements SocketCallback {

    private static final int PORT = 22299;
    private static final String appName = "junit";
    private static final String appVersion = "1.0";
    private static final String serviceName = "service name";
    private static final String serviceDescription = "description";
    private static final String serviceDescription2 = "description_longer";

    private AtomicBoolean gotConnection;

    @Override
    public void newClient(Socket socket) {
        gotConnection.set(true);
    }

    @Before
    public void setUp() {
        gotConnection = new AtomicBoolean();
    }

    @Test(timeout = 60000)
    public void testDiscoveryAndConnect() {

        // Create a service
        LocalBroadcastDiscoveryServer discoveryServer = new LocalBroadcastDiscoveryServer(PORT, 1000);
        DiscoverableService discoverableService = new DiscoverableService(appName, appVersion, serviceName, serviceDescription);
        discoveryServer.addTcpBroadcastService(discoverableService, this);
        Assert.assertTrue(discoverableService.toString().startsWith("junit|1.0|service name|description|TCP|"));

        // Try to retrieve the service
        LocalBroadcastDiscoveryClient discoveryClient = new LocalBroadcastDiscoveryClient(PORT);
        List<DiscoverableService> servicesList = discoveryClient.retrieveServicesList();
        while (servicesList.isEmpty()) {
            ThreadTools.sleep(100);
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

        discoveryServer.shutdown();
        discoveryClient.shutdown();
    }

    @Test(timeout = 60000)
    public void testDiscoveryManyServices() {

        // Create a service
        LocalBroadcastDiscoveryClient discoveryClient = new LocalBroadcastDiscoveryClient(PORT);
        LocalBroadcastDiscoveryServer discoveryServer = new LocalBroadcastDiscoveryServer(PORT, 1000);
        discoveryServer.addTcpBroadcastService(new DiscoverableService(appName, appVersion, serviceName, serviceDescription2), this);
        discoveryServer.addTcpBroadcastService(new DiscoverableService(appName, appVersion, serviceName, serviceDescription), this);

        // Try to retrieve the services
        List<DiscoverableService> servicesList = discoveryClient.retrieveServicesList();
        while (servicesList.size() < 2) {
            ThreadTools.sleep(100);
            servicesList = discoveryClient.retrieveServicesList();
        }

        // Check this is the right services
        Assert.assertEquals(2, servicesList.size());
        servicesList.sort((o1, o2) -> o1.getServiceDescription().compareTo(o2.getServiceDescription()));
        DiscoverableService discoverableServiceRetrieved = servicesList.get(0);
        Assert.assertEquals(appName, discoverableServiceRetrieved.getAppName());
        Assert.assertEquals(appVersion, discoverableServiceRetrieved.getAppVersion());
        Assert.assertEquals(serviceName, discoverableServiceRetrieved.getServiceName());
        Assert.assertEquals(serviceDescription, discoverableServiceRetrieved.getServiceDescription());
        Assert.assertEquals(DiscoverableService.TCP, discoverableServiceRetrieved.getServerType());
        Assert.assertTrue(discoverableServiceRetrieved.getServerPort() > 0);

        discoverableServiceRetrieved = servicesList.get(1);
        Assert.assertEquals(appName, discoverableServiceRetrieved.getAppName());
        Assert.assertEquals(appVersion, discoverableServiceRetrieved.getAppVersion());
        Assert.assertEquals(serviceName, discoverableServiceRetrieved.getServiceName());
        Assert.assertEquals(serviceDescription2, discoverableServiceRetrieved.getServiceDescription());
        Assert.assertEquals(DiscoverableService.TCP, discoverableServiceRetrieved.getServerType());
        Assert.assertTrue(discoverableServiceRetrieved.getServerPort() > 0);

        discoveryServer.shutdown();
        discoveryClient.shutdown();
    }

}
