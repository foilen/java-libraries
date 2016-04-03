/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.communication.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.event.EventCallback;
import com.foilen.smalltools.event.EventList;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.commander.CommanderServer;
import com.foilen.smalltools.net.communication.CommunicationCommand;
import com.foilen.smalltools.net.communication.CommunicationCommandExecutor;
import com.foilen.smalltools.net.communication.CommunicationSystem;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionAction;
import com.foilen.smalltools.net.connections.ConnectionAssemblyLine;
import com.foilen.smalltools.net.connections.ConnectionFactory;
import com.foilen.smalltools.net.connections.ConnectionMessageConstants;
import com.foilen.smalltools.net.connections.factory.HostPortConnectionFactory;
import com.foilen.smalltools.net.services.TCPServerService;

/**
 * This is a simple system to send serialized messages that processes themselves (command pattern). Make your messages types implements {@link CommunicationCommand}.
 * 
 * Defaults:
 * 
 * <pre>
 * - Uses "host:port" to create outgoing connections
 * - Uses YAML for the serialization
 * - There are 5 threads that executes the incoming messages (messagesExecutorThreadsAmount)
 * </pre>
 * 
 * Usage:
 * 
 * <pre>
 * SimpleCommunicationSystem simpleCommunicationSystem = new SimpleCommunicationSystem();
 * 
 * // If you want a server
 * simpleCommunicationSystem.setServer(true);
 * simpleCommunicationSystem.setServerPort(9999); // Optional (will take any available port)
 * 
 * simpleCommunicationSystem.init();
 * 
 * // Send a message
 * String mainMessage = &quot;Hello World&quot;;
 * if (simpleCommunicationSystem.sendObject(&quot;127.0.0.1:9191&quot;, mainMessage)) {
 *     System.out.println(&quot;Message sent&quot;);
 * } else {
 *     System.out.println(&quot;Could not send the message&quot;);
 * }
 * 
 * // To receive the messages. Use a dedicated thread
 * Map&lt;String, Object&gt; nextMessage = simpleCommunicationSystem.getNextMessage();
 * String messageContent = (String) nextMessage.get(ConnectionMessageConstants.OBJECT);
 * System.out.println(&quot;Received message: &quot; + messageContent);
 * // Send back a reply
 * Connection sender = (Connection) nextMessage.get(ConnectionMessageConstants.CONNECTION);
 * sender.sendObject(&quot;Thanks&quot;);
 * </pre>
 * 
 * Deprecated: Use {@link CommanderServer} and {@link CommanderClient} instead
 */
@Deprecated
public class SimpleCommunicationSystem implements CommunicationSystem {

    private final static Logger log = LoggerFactory.getLogger(SimpleCommunicationSystem.class);

    // Services
    private ExecutorService executorService = new ThreadPoolExecutor(1, 10, 20L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(200));
    private ConnectionFactory connectionFactory = new HostPortConnectionFactory();
    private ConnectionAssemblyLine incomingAssemblyLine = new ConnectionAssemblyLine();
    private ConnectionAssemblyLine outgoingAssemblyLine = new ConnectionAssemblyLine();
    private EventList<Connection> eventsConnected = new EventList<>();
    private EventList<Connection> eventsDisconnected = new EventList<>();
    private EventList<Connection> eventsMessageReceived = new EventList<>();
    private BlockingQueue<Map<String, Object>> incomingMessagesQueue = new ArrayBlockingQueue<>(1000);

    // Config
    private boolean server;
    private Integer serverPort;
    private int messagesExecutorThreadsAmount = 5;

    // Running
    private TCPServerService tcpServerService;
    private Map<String, Connection> connectionById = new ConcurrentHashMap<>();
    private Map<Connection, SimpleMessageSender> messageSenderByConnection = new ConcurrentHashMap<>();

    @Override
    public void addConnectionAction(ConnectionAction connectionAction) {
        log.debug("Adding a connection action to both channels of type {}", connectionAction.getClass().getName());
        incomingAssemblyLine.addAction(connectionAction);
        outgoingAssemblyLine.addAction(connectionAction);
    }

    @Override
    public void addConnectionActionToIncoming(ConnectionAction connectionAction) {
        log.debug("Adding a connection action to incoming channel of type {}", connectionAction.getClass().getName());
        incomingAssemblyLine.addAction(connectionAction);
    }

    @Override
    public void addConnectionActionToOutgoing(ConnectionAction connectionAction) {
        log.debug("Adding a connection action to outgoing channel of type {}", connectionAction.getClass().getName());
        outgoingAssemblyLine.addAction(connectionAction);
    }

    @Override
    public void addEventConnectedCallback(EventCallback<Connection> callback) {
        log.debug("Adding an event to the connected event {}", callback.getClass().getName());
        eventsConnected.addCallback(callback);
    }

    @Override
    public void addEventDisconnectedCallback(EventCallback<Connection> callback) {
        log.debug("Adding an event to the disconnected event {}", callback.getClass().getName());
        eventsDisconnected.addCallback(callback);
    }

    @Override
    public void addEventMessageReceivedCallback(EventCallback<Connection> callback) {
        log.debug("Adding an event to the message received event {}", callback.getClass().getName());
        eventsMessageReceived.addCallback(callback);
    }

    /**
     * Add a new incoming message to the queue and send an event.
     * 
     * @param message
     *            the message
     */
    protected void addNewMessage(Map<String, Object> message) {
        log.debug("Adding a new message to the queue: {}", message);

        // Add
        incomingMessagesQueue.add(message);

        // Event
        eventsMessageReceived.dispatch((Connection) message.get(ConnectionMessageConstants.CONNECTION));
    }

    @Override
    public Connection connectTo(String id) {
        log.debug("Getting a connection to {}", id);

        // Get from already connected
        Connection connection = connectionById.get(id);
        if (connection != null) {
            log.debug("Already connected to {}. Returning that connection", id);
            return connection;
        }

        // Get from the connection factory
        log.debug("Not already connected to {}. Getting a new connection from the factory", id);
        connection = connectionFactory.createConnection(id);
        if (connection == null) {
            log.warn("Could not connect to {}", id);
            return null;
        }
        connection.setId(id);

        // Go through the gates
        return registerOutgoingConnection(connection);

    }

    /**
     * Remove the connection.
     * 
     * @param connection
     *            the connection
     */
    protected void disconnect(Connection connection) {

        log.debug("Disconnecting connection with id {}", connection.getId());

        // Remove if present
        Iterator<Entry<String, Connection>> it = connectionById.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Connection> entry = it.next();
            if (entry.getValue() == connection) {
                it.remove();
            }
        }
        SimpleMessageSender messageSender = messageSenderByConnection.get(connection);
        if (messageSender != null) {
            messageSender.requestStop();
            messageSenderByConnection.remove(connection);
        }

        // Close
        connection.close();

        // Event
        eventsDisconnected.dispatch(connection);
    }

    @Override
    public void disconnect(String id) {
        log.debug("Disconnecting any connection with id {}", id);

        Connection connection = connectionById.get(id);
        if (connection != null) {
            disconnect(connection);
        }
    }

    @Override
    public void disconnectAfterMessagesSent() {
        log.debug("Sending disconnection instruction to all the message senders");

        // Get the message senders
        List<SimpleMessageSender> messageSenders = new ArrayList<SimpleMessageSender>();
        messageSenders.addAll(messageSenderByConnection.values());

        // Request disconnects
        for (SimpleMessageSender messageSender : messageSenders) {
            messageSender.addMessageToQueue(SimpleMessageSender.DISCONNECT_MESSAGE);
        }

        // Wait for completion
        for (SimpleMessageSender messageSender : messageSenders) {
            try {
                messageSender.join();
            } catch (InterruptedException e) {
                log.error("Waiting for disconnection interrupted", e);
            }
        }
    }

    @Override
    public void disconnectAfterMessagesSent(String id) {
        log.debug("Sending disconnection instruction to the message sender of the connection id {}", id);

        // Get the connection
        Connection connection = connectionById.get(id);
        if (connection == null) {
            log.warn("The connection id {} is not connected. Cannot request disconnection", id);
            return;
        }

        // Get the message sender
        SimpleMessageSender messageSender = messageSenderByConnection.get(connection);
        if (messageSender == null) {
            log.warn("The connection id {} is not connected. Cannot request disconnection", id);
        } else {
            // Request disconnection after the last message is sent
            messageSender.addMessageToQueue(SimpleMessageSender.DISCONNECT_MESSAGE);
            // Wait for completion
            try {
                messageSender.join();
            } catch (InterruptedException e) {
                log.error("Waiting for disconnection interrupted", e);
            }
        }
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public int getMessagesExecutorThreadsAmount() {
        return messagesExecutorThreadsAmount;
    }

    @Override
    public Map<String, Object> getNextMessage() {
        log.debug("Getting next available received message");
        try {
            return incomingMessagesQueue.take();
        } catch (InterruptedException e) {
            throw new SmallToolsException("Problem getting the next message", e);
        }
    }

    /**
     * Get the server port.
     * 
     * @return the server port
     */
    public Integer getServerPort() {
        return serverPort;
    }

    /**
     * Call to start all the initial threads and configure everything.
     */
    public void init() {

        log.info("Initializing the communication system");

        // Server
        if (tcpServerService != null) {
            throw new SmallToolsException("The server has already been initialized");
        }

        if (server) {
            log.debug("Is a server");
            if (serverPort == null) {
                tcpServerService = new TCPServerService(new SimpleIncomingConnection(this));
            } else {
                tcpServerService = new TCPServerService(serverPort, new SimpleIncomingConnection(this));
            }

            serverPort = tcpServerService.getPort();
            log.info("Server listening on port {}", serverPort);
        }

        // Queue processors
        for (int i = 0; i < messagesExecutorThreadsAmount; ++i) {
            new CommunicationCommandExecutor(this);
        }

        log.info("Initialization completed");
    }

    /**
     * Tells if is a server.
     * 
     * @return true if is a server
     */
    public boolean isServer() {
        return server;
    }

    /**
     * Add connection to the pool and dispatch a connection event. If there is an already existing connection, disconnect it.
     * 
     * @param connection
     *            the final connection
     */
    protected void registerConnection(Connection connection) {

        log.debug("Registering a new connection {}", connection);

        String id = connection.getId();

        // Validate
        if (id == null) {
            log.error("Cannot register the anonymous connection (has no ID)");
            return;
        }

        // Add
        Connection oldConnection = connectionById.put(id, connection);
        if (oldConnection != null) {
            disconnect(oldConnection);
        }

        // Message sender
        messageSenderByConnection.put(connection, new SimpleMessageSender(this, connection));

        // Event
        eventsConnected.dispatch(connection);

        // Add a listener
        new SimpleMessageReader(this, connection);
    }

    /**
     * Process the connection with the incoming actions and register it if succeed.
     * 
     * @param connection
     *            the new incoming connection
     */
    protected void registerIncomingConnection(Connection connection) {

        log.debug("Trying to register a new incoming connection {}", connection);

        if (connection == null) {
            return;
        }

        Connection finalConnection = incomingAssemblyLine.process(connection);

        if (finalConnection == null) {
            log.debug("The incoming connection {} did not passed the gates", connection);
        } else {
            // If there is no id: ask connection factory
            if (finalConnection.getId() == null) {
                log.debug("The incoming connection {} does not have an id. Requesting one from the factory", finalConnection);
                finalConnection.setId(connectionFactory.generateId(finalConnection));
            }

            registerConnection(finalConnection);
        }
    }

    /**
     * Process the connection with the outgoing actions and register it if succeed.
     * 
     * @param connection
     *            the new outgoing connection
     * @return the final connection or null if failed
     */
    protected Connection registerOutgoingConnection(Connection connection) {

        log.debug("Trying to register a new outgoing connection {}", connection);

        if (connection == null) {
            return null;
        }

        Connection finalConnection = outgoingAssemblyLine.process(connection);
        if (finalConnection == null) {
            log.debug("The outgoing connection {} did not passed the gates", connection);
        } else {
            registerConnection(finalConnection);
        }
        return finalConnection;
    }

    @Override
    public void sendMessage(Map<String, Object> message) {
        log.debug("Sending a message to all connections {}", message);

        Iterator<Entry<Connection, SimpleMessageSender>> entryId = messageSenderByConnection.entrySet().iterator();
        while (entryId.hasNext()) {
            SimpleMessageSender messageSender = entryId.next().getValue();
            messageSender.addMessageToQueue(message);
        }
    }

    @Override
    public boolean sendMessage(String id, Map<String, Object> message) {

        log.debug("Sending a message to {}: {}", id, message);

        // Get the connection
        Connection connection = connectTo(id);
        if (connection == null) {
            return false;
        }

        // Get the sender
        SimpleMessageSender messageSender = messageSenderByConnection.get(connection);
        if (messageSender == null) {
            return false;
        }
        messageSender.addMessageToQueue(message);

        return true;
    }

    @Override
    public void sendObject(Object object) {
        log.debug("Sending an object to all connections {}", object);

        Map<String, Object> message = new HashMap<>();
        message.put(ConnectionMessageConstants.OBJECT, object);
        sendMessage(message);
    }

    @Override
    public boolean sendObject(String id, Object object) {
        log.debug("Sending an object to {}: {}", id, object);

        Map<String, Object> message = new HashMap<>();
        message.put(ConnectionMessageConstants.OBJECT, object);
        return sendMessage(id, message);
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Choose how many threads will execute the incoming messages.
     * 
     * @param messagesExecutorThreadsAmount
     *            the amount of threads
     */
    public void setMessagesExecutorThreadsAmount(int messagesExecutorThreadsAmount) {
        this.messagesExecutorThreadsAmount = messagesExecutorThreadsAmount;
    }

    /**
     * Set to true to activate a server. Set a port with {@link #setServerPort(Integer)} if you want to specify one.
     * 
     * @param server
     *            true to initialize the server when {@link #init()}
     */
    public void setServer(boolean server) {
        this.server = server;
    }

    /**
     * Set the server port.
     * 
     * @param serverPort
     *            the server port or null to take any available
     */
    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

}
