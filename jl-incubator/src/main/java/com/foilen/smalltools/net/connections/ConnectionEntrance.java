/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.connections.factory.HostPortConnectionFactory;
import com.foilen.smalltools.net.services.SocketCallback;
import com.foilen.smalltools.net.services.TCPServerService;

/**
 * This is the entry point in the connection system. This system is an easy way to pass messages over a TCP connection. It is easy to add security to the connection.
 * 
 * <pre>
 * Here are the important parts of this system:
 * - The {@link ConnectionEntrance} is where you can create a server and initiate connections to remote server.
 * - After a connection is established (incoming or outgoing), some actions are done in the {@link ConnectionHallway}. This is where a password can be requested and encryption can be negotiated.
 * - When the connection is fine, it is stored in the {@link ConnectionsRoom} until they are disconnected.
 * </pre>
 * 
 * <pre>
 * What happens when you request a remote connection here:
 * - It checks in the {@link ConnectionsRoom} if a connection is already present.
 * - If it is not, it uses the provided {@link ConnectionFactory} to initiate one.
 * - The connection is returned if it did not fail in the {@link ConnectionHallway}.
 * </pre>
 * 
 * TODO --- Example of client/server
 */
public class ConnectionEntrance {

    /**
     * Take the incoming connections and send them to the {@link ConnectionHallway}.
     */
    protected class IncomingConnection implements SocketCallback {
        @Override
        public void newClient(Socket socket) {
            Connection connection = new Connection(socket);
            executorService.execute(new IncomingConnectionProcess(connection));
        }
    }

    protected class IncomingConnectionProcess implements Runnable {

        private Connection connection;

        public IncomingConnectionProcess(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            registerIfNotNull(connectionHallway.processIncomingConnection(connection));
        }
    }

    private ExecutorService executorService = new ThreadPoolExecutor(1, 10, 20L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(200));

    private ConnectionFactory connectionFactory = new HostPortConnectionFactory();

    private TCPServerService tcpServerService;

    private ConnectionHallway connectionHallway = new ConnectionHallway();
    private ConnectionsRoom connectionsRoom = new ConnectionsRoom();
    private ConnectionsInteractions connectionsInteractions = new ConnectionsInteractions();

    /**
     * Get or create a connection. It will try to get an existing one in the {@link ConnectionsRoom} or create one with the {@link ConnectionFactory}.
     * 
     * <pre>
     * Warning, creating a connection can take multiple seconds (the time it goes through the {@link ConnectionHallway}).
     * </pre>
     * 
     * @param id
     *            the id of the connection
     * @return the connection or null if could not connect
     */
    public Connection connectTo(String id) {
        Connection connection = connectionsRoom.getConnectionById(id);
        if (connection == null) {
            connection = connectionFactory.createConnection(id);
            if (connection == null) {
                return null;
            }
            connection = registerIfNotNull(connectionHallway.processOutgoingConnection(connection));
        }
        return connection;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public ConnectionHallway getConnectionHallway() {
        return connectionHallway;
    }

    public ConnectionsInteractions getConnectionsInteractions() {
        return connectionsInteractions;
    }

    public ConnectionsRoom getConnectionsRoom() {
        return connectionsRoom;
    }

    /**
     * Create a TCP server with a random port number that you can retrieve on the {@link TCPServerService#getPort()}.
     * 
     * @return the TCP Server
     */
    public TCPServerService initServer() {
        return initServer(null);
    }

    /**
     * Create a TCP server.
     * 
     * @param port
     *            the port number
     * @return the TCP Server
     */
    public TCPServerService initServer(Integer port) {
        if (tcpServerService != null) {
            throw new SmallToolsException("This entrance already has a server initialized");
        }

        if (port == null) {
            tcpServerService = new TCPServerService(new IncomingConnection());
        } else {
            tcpServerService = new TCPServerService(port, new IncomingConnection());
        }

        return tcpServerService;
    }

    /**
     * If the connection is set, adds it to the room and start the interlocutor.
     * 
     * @param connection
     *            the connection
     * @return the connection
     */
    private Connection registerIfNotNull(Connection connection) {
        if (connection != null) {
            connectionsRoom.addConnection(connection);
            connection.activateInterlocutor(connectionsInteractions);
        }
        return connection;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setConnectionHallway(ConnectionHallway connectionHallway) {
        this.connectionHallway = connectionHallway;
    }

    public void setConnectionsInteractions(ConnectionsInteractions connectionsInteractions) {
        this.connectionsInteractions = connectionsInteractions;
    }

    public void setConnectionsRoom(ConnectionsRoom connectionsRoom) {
        this.connectionsRoom = connectionsRoom;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

}
