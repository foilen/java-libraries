/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections;

/**
 * This is the way to create an outgoing connection.
 */
public interface ConnectionFactory {

    /**
     * Create a connection using the id.
     * 
     * @param id
     *            the id that can be understood by this factory
     * @return the connection
     */
    Connection createConnection(String id);

}
