package com.foilen.smalltools.net.communication.simple;

import java.util.HashMap;
import java.util.Iterator;
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
 */
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
        incomingAssemblyLine.addAction(connectionAction);
        outgoingAssemblyLine.addAction(connectionAction);
    }

    @Override
    public void addConnectionActionToIncoming(ConnectionAction connectionAction) {
        incomingAssemblyLine.addAction(connectionAction);
    }

    @Override
    public void addConnectionActionToOutgoing(ConnectionAction connectionAction) {
        outgoingAssemblyLine.addAction(connectionAction);
    }

    @Override
    public void addEventConnectedCallback(EventCallback<Connection> callback) {
        eventsConnected.addCallback(callback);
    }

    @Override
    public void addEventDisconnectedCallback(EventCallback<Connection> callback) {
        eventsDisconnected.addCallback(callback);
    }

    @Override
    public void addEventMessageReceivedCallback(EventCallback<Connection> callback) {
        eventsMessageReceived.addCallback(callback);
    }

    /**
     * Add a new incoming message to the queue and send an event.
     * 
     * @param message
     *            the message
     */
    protected void addNewMessage(Map<String, Object> message) {
        // Add
        incomingMessagesQueue.add(message);

        // Event
        eventsMessageReceived.dispatch((Connection) message.get(ConnectionMessageConstants.CONNECTION));
    }

    @Override
    public Connection connectTo(String id) {
        // Get from already connected
        Connection connection = connectionById.get(id);
        if (connection == null) {
            // Get from the connection factory
            connection = connectionFactory.createConnection(id);
            connection.setId(id);
        }
        return registerOutgoingConnection(connection);
    }

    /**
     * Remove the connection.
     * 
     * @param connection
     *            the connection
     */
    protected void disconnect(Connection connection) {

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
        Connection connection = connectionById.get(id);
        if (connection != null) {
            disconnect(connection);
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

        // Server
        if (tcpServerService != null) {
            throw new SmallToolsException("The server has already been initialized");
        }

        if (server) {
            if (serverPort == null) {
                tcpServerService = new TCPServerService(new SimpleIncomingConnection(this));
            } else {
                tcpServerService = new TCPServerService(serverPort, new SimpleIncomingConnection(this));
            }

            serverPort = tcpServerService.getPort();
        }

        // Queue processors
        for (int i = 0; i < messagesExecutorThreadsAmount; ++i) {
            new CommunicationCommandExecutor(this);
        }
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

        // Event
        eventsConnected.dispatch(connection);

        // Add a listener
        new SimpleMessageListener(this, connection);
    }

    /**
     * Process the connection with the incoming actions and register it if succeed.
     * 
     * @param connection
     *            the new incoming connection
     */
    protected void registerIncomingConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        Connection finalConnection = incomingAssemblyLine.process(connection);
        if (finalConnection != null) {
            // If there is no id: ask connection factory
            if (finalConnection.getId() == null) {
                finalConnection.setId(connectionFactory.generateId(connection));
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
        if (connection == null) {
            return null;
        }

        Connection finalConnection = outgoingAssemblyLine.process(connection);
        if (finalConnection != null) {
            registerConnection(finalConnection);
        }
        return finalConnection;
    }

    @Override
    public void sendMessage(Map<String, Object> message) {
        Iterator<Entry<Connection, SimpleMessageSender>> entryId = messageSenderByConnection.entrySet().iterator();
        while (entryId.hasNext()) {
            SimpleMessageSender messageSender = entryId.next().getValue();
            messageSender.addMessageToQueue(message);
        }
    }

    @Override
    public boolean sendMessage(String id, Map<String, Object> message) {

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
        Map<String, Object> message = new HashMap<>();
        message.put(ConnectionMessageConstants.OBJECT, object);
        sendMessage(message);
    }

    @Override
    public boolean sendObject(String id, Object object) {
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
