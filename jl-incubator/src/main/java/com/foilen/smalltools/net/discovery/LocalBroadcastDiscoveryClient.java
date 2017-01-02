/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

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
import com.foilen.smalltools.tools.CloseableTools;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A client that can automatically discover servers on a LAN. Upon instantiation, a new thread is started right away. The list of discovered servers is kept in memory for 1 minute by default.
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
 * 
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:18.0'
 * </pre>
 */
public class LocalBroadcastDiscoveryClient implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LocalBroadcastDiscoveryClient.class);

    private static final int CACHE_EXPIRE_IN_SECONDS = 60;
    private static final long CACHE_MAX_SIZE = 1000;

    private Cache<DiscoverableService, Boolean> localDiscoveryServices;

    private Thread thread;
    private String filteredAppName;
    private String filteredAppVersion;
    private DatagramSocket datagramSocket;

    /**
     * Create the client using the default cache.
     * 
     * @param port
     *            the port to watch for the messages
     */
    public LocalBroadcastDiscoveryClient(int port) {
        localDiscoveryServices = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE).expireAfterWrite(CACHE_EXPIRE_IN_SECONDS, TimeUnit.SECONDS).build();
        init(port);
    }

    /**
     * Create the client and specify the cache.
     * 
     * @param port
     *            the port to watch for the messages
     * @param cacheMaxSize
     *            the maximum amount of entries to remember
     * @param cacheExpireInSeconds
     *            how long to keep an entry that is no more broadcasted
     */
    public LocalBroadcastDiscoveryClient(int port, long cacheMaxSize, long cacheExpireInSeconds) {
        localDiscoveryServices = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterWrite(cacheExpireInSeconds, TimeUnit.SECONDS).build();
        init(port);
    }

    /**
     * Create the client using the default cache.
     * 
     * @param port
     *            the port to watch for the messages
     * @param filteredAppName
     *            the name of the application to monitor
     * @param filteredAppVersion
     *            the version of the application to monitor
     */
    public LocalBroadcastDiscoveryClient(int port, String filteredAppName, String filteredAppVersion) {

        localDiscoveryServices = CacheBuilder.newBuilder().maximumSize(CACHE_MAX_SIZE).expireAfterWrite(CACHE_EXPIRE_IN_SECONDS, TimeUnit.SECONDS).build();

        this.filteredAppName = filteredAppName;
        this.filteredAppVersion = filteredAppVersion;

        init(port);
    }

    /**
     * Create the client and specify the cache.
     * 
     * @param port
     *            the port to watch for the messages
     * @param filteredAppName
     *            the name of the application to monitor
     * @param filteredAppVersion
     *            the version of the application to monitor
     * @param cacheMaxSize
     *            the maximum amount of entries to remember
     * @param cacheExpireInSeconds
     *            how long to keep an entry that is no more broadcasted
     */
    public LocalBroadcastDiscoveryClient(int port, String filteredAppName, String filteredAppVersion, long cacheMaxSize, long cacheExpireInSeconds) {

        localDiscoveryServices = CacheBuilder.newBuilder().maximumSize(cacheMaxSize).expireAfterWrite(cacheExpireInSeconds, TimeUnit.SECONDS).build();

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
        thread = new Thread(this, "LocalBroadcastDiscoveryClient-" + port);
        thread.setDaemon(true);
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

        List<DiscoverableService> result = new ArrayList<>();

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

        while (datagramSocket != null) {
            try {

                // Get a message
                datagramSocket.receive(packet);
                if (logger.isDebugEnabled()) {
                    logger.debug("[{}] Got message: {}", packet.getAddress().getHostName(), new String(buf, 0, packet.getLength()));
                }
                DiscoverableService localDiscoveryService = new DiscoverableService(buf, 0, packet.getLength());
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
                // Show error if should be running
                if (datagramSocket != null) {
                    logger.error("Problem receiving broadcast message", e);
                }
            }
        }
    }

    /**
     * To stop the service.
     */
    public void shutdown() {
        DatagramSocket tmp = datagramSocket;
        datagramSocket = null;
        CloseableTools.close(tmp);
    }

}
