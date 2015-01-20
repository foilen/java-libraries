/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.services.SocketCallback;
import com.foilen.smalltools.net.services.TCPServerService;

/**
 * A broadcasting service to use on a LAN. This service is sending an UDP broadcast message on the local network at a certain interval. The message includes the type of service and how to connect to
 * it. There can be multiple services. Upon instantiation, a new thread is started right away.
 * 
 * Defaults:
 * 
 * <pre>
 * <ul>
 * <li>Broadcast delay: 5 seconds between broadcasts</li>
 * </ul>
 * </pre>
 * 
 * Usage:
 * 
 * <pre>
 * // Create a service
 * LocalBroadcastDiscoveryServer discoveryServer = new LocalBroadcastDiscoveryServer(9999);
 * DiscoverableService discoverableService = new DiscoverableService(appName, appVersion, serviceName, serviceDescription);
 * discoveryServer.addTcpBroadcastService(discoverableService, mySocketCallback);
 * </pre>
 */
public class LocalBroadcastDiscoveryServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LocalBroadcastDiscoveryServer.class.getName());

    // Properties
    private int broadcastDelay = 5000;

    // Internals
    private Thread thread;
    private DatagramSocket datagramSocket;
    private List<byte[]> messages = new CopyOnWriteArrayList<byte[]>();

    public LocalBroadcastDiscoveryServer(int port) {
        init(port);
    }

    public LocalBroadcastDiscoveryServer(int port, int broadcastDelay) {
        this.broadcastDelay = broadcastDelay;
        init(port);
    }

    /**
     * Create a new TCP server on any available port and start broadcasting it.
     * 
     * @param discoverableService
     *            the details about the service. The server type and port will be set.
     * @param socketCallback
     *            when someone connects to this new server, this handler will be called
     * @return the {@link TCPServerService}
     */
    public TCPServerService addTcpBroadcastService(DiscoverableService discoverableService, SocketCallback socketCallback) {
        return addTcpBroadcastService(discoverableService, new TCPServerService(socketCallback));
    }

    /**
     * Use an existing TCP server and start broadcasting it.
     * 
     * @param discoverableService
     *            the details about the service. The server type and port will be set.
     * @param socketCallback
     *            when someone connects to this new server, this handler will be called
     * @param serverSocket
     *            the already created TCP server
     * @return the {@link TCPServerService}
     */
    public TCPServerService addTcpBroadcastService(DiscoverableService discoverableService, SocketCallback socketCallback, ServerSocket serverSocket) {
        return addTcpBroadcastService(discoverableService, new TCPServerService(socketCallback, serverSocket));
    }

    /**
     * Use an existing TCP server and start broadcasting it.
     * 
     * @param discoverableService
     *            the details about the service. The server type and port will be set.
     * @param tcpServerService
     *            the already created TCP server
     * @return the {@link TCPServerService} (the same that was passed in the parameter)
     */
    public TCPServerService addTcpBroadcastService(DiscoverableService discoverableService, TCPServerService tcpServerService) {

        // Set the port on the message
        discoverableService.setServerType("TCP");
        discoverableService.setServerPort(tcpServerService.getPort());

        // Register message
        byte[] bytes = discoverableService.toBytes();
        logger.info("New TCP broadcast service {}", new String(bytes));
        messages.add(bytes);

        return tcpServerService;
    }

    /**
     * The delay between each broadcast.
     * 
     * @return the broadcastDelay
     */
    public int getBroadcastDelay() {
        return broadcastDelay;
    }

    private void init(int port) {
        // Create the broadcast
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);
            datagramSocket.connect(InetAddress.getByName("255.255.255.255"), port);
        } catch (Exception e) {
            logger.error( "Error binding broadcast", e);
            throw new SmallToolsException(e);
        }

        // Start the thread
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Remove all the broadcast messages.
     */
    public void removeAllMessages() {
        messages.clear();
    }

    /**
     * Broadcast the services at a regular interval.
     */
    @Override
    public void run() {

        logger.info("Starting broadcasting on port {}", datagramSocket.getPort());

        // Wait before starting
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.error( "Waiting interrupted", e);
        }

        while (true) {
            logger.debug("Broadcasting {} messages", messages.size());
            for (byte[] message : messages) {
                // Broadcast
                DatagramPacket packet = new DatagramPacket(message, message.length);
                try {
                    datagramSocket.send(packet);
                } catch (IOException e) {
                    logger.error( "Could not broadcast message " + new String(message), e);
                }

                // Wait
                try {
                    Thread.sleep(broadcastDelay);
                } catch (InterruptedException e) {
                    logger.error( "Waiting interrupted", e);
                }
            }
        }
    }

    /**
     * The delay between each broadcast.
     * 
     * @param broadcastDelay
     *            the broadcastDelay to set
     */
    public void setBroadcastDelay(int broadcastDelay) {
        this.broadcastDelay = broadcastDelay;
    }
}
