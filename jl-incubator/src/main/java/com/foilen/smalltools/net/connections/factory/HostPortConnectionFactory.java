/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.factory;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionFactory;
import com.foilen.smalltools.net.connections.ConnectionLanguage;

/**
 * Create connections with the "host:port" form. E.g. "foilen.com:80".
 */
public class HostPortConnectionFactory implements ConnectionFactory {

    private static Logger logger = LoggerFactory.getLogger(HostPortConnectionFactory.class);
    private ConnectionLanguage connectionLanguage;

    @Override
    public Connection createConnection(String id) {
        String[] parts = id.split(":");

        if (parts.length != 2) {
            logger.warn("{} is an invalid format. Expected host:port");
            return null;
        }

        try {
            int port = Integer.valueOf(parts[1]);
            Socket socket = new Socket(parts[0], port);
            if (connectionLanguage == null) {
                return new Connection(socket);
            } else {
                return new Connection(socket, connectionLanguage);
            }
        } catch (Exception e) {
            logger.warn("Problem connecting", e);
        }

        return null;

    }

    @Override
    public String generateId(Connection connection) {
        return connection.getInetAddress().getHostAddress() + ":" + connection.getPort();
    }

    /**
     * If you want to change the connection language instead of using the default one.
     * 
     * @param connectionLanguage
     *            the language
     */
    public void setConnectionLanguage(ConnectionLanguage connectionLanguage) {
        this.connectionLanguage = connectionLanguage;
    }

}
