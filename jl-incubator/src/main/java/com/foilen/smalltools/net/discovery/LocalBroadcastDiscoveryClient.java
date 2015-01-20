/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A client that can automatically discover servers on a LAN. Upon instantiation, a new thread is started right away. The list of discovered servers is kept in memory for 1 minute.
 * 
 * Usage:
 * 
 * <pre>
 * // Start the discovery
 * LocalBroadcastDiscoveryClient discoveryClient = new LocalBroadcastDiscoveryClient(9999);
 * 
 * // Retrieve the services that were seen
 * List&lt;DiscoverableService&gt; services = discoveryClient.retrieveServicesList();
 * for (DiscoverableService service : services) {
 *     // Try to connect
 *     Socket socket = service.connecToTcpService();
 *     // ...
 * }
 * </pre>
 */
public class LocalBroadcastDiscoveryClient implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LocalBroadcastDiscoveryClient.class.getName());

    private static final int CACHE_EXPIRE_IN_SECONDS = 60;
    private static final long CACHE_MAX_SIZE = 1000;

    private Cache<DiscoverableService, Boolean> localDiscoveryServices = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE).expireAfterWrite(CACHE_EXPIRE_IN_SECONDS, TimeUnit.SECONDS).build();

    private Thread thread;
    private String filteredAppName;
    private String filteredAppVersion;
    private DatagramSocket datagramSocket;

    /**
     * @param port
     *            the port to watch for the messages
     */
    public LocalBroadcastDiscoveryClient(int port) {
        init(port);
    }

    /**
     * 
     * @param port
     *            the port to watch for the messages
     * @param filteredAppName
     *            the name of the application to monitor
     * @param filteredAppVersion
     *            the version of the application to monitor
     */
    public LocalBroadcastDiscoveryClient(int port, String filteredAppName, String filteredAppVersion) {

        this.filteredAppName = filteredAppName;
        this.filteredAppVersion = filteredAppVersion;

        init(port);
    }

    private void init(int port) {

        // Create the broadcast
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (Exception e) {
            logger.error("Error binding socket", e);
            throw new SmallToolsException(e);
        }

        // Start the thread
        thread = new Thread(this);
        thread.start();
        logger.info("Starting listening to broadcast on port {}", port);
    }

    /**
     * Get the list of services.
     * 
     * @return the list of available services
     */
    public List<DiscoverableService> retrieveServicesList() {
        return retrieveServicesList(null);
    }

    /**
     * Get the list of services.
     * 
     * @param filteredServiceName
     *            filter by service type
     * @return the list of available services
     */
    public List<DiscoverableService> retrieveServicesList(String filteredServiceName) {

        List<DiscoverableService> result = new ArrayList<DiscoverableService>();

        for (DiscoverableService service : localDiscoveryServices.asMap().keySet()) {
            if (filteredServiceName == null || service.getServiceName().equals(filteredServiceName)) {
                result.add(service);
            }
        }

        return result;
    }

    @Override
    public void run() {

        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (true) {
            try {

                // Get a message
                datagramSocket.receive(packet);
                DiscoverableService localDiscoveryService = new DiscoverableService(buf);
                localDiscoveryService.setServerHost(packet.getAddress().getHostName());

                // Filter it if needed
                if ((filteredAppName != null && !localDiscoveryService.getAppName().equals(filteredAppName))
                        || (filteredAppVersion != null && !localDiscoveryService.getAppVersion().equals(filteredAppVersion))) {
                    continue;
                }

                // Cache the service
                localDiscoveryServices.put(localDiscoveryService, true);

                logger.debug("Found service {}", localDiscoveryService);

            } catch (Exception e) {
                logger.error("Problem receiving broadcast message", e);
            }
        }
    }

}
