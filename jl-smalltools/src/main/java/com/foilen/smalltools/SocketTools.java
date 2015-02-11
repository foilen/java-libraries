/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import java.net.SocketException;

/**
 * Some simple methods to play with sockets.
 */
public final class SocketTools {

    public static boolean isADisconnectionException(Throwable t) {

        if (t == null) {
            return false;
        }

        // Current
        if (t instanceof SocketException) {
            if ("Connection reset".equals(t.getMessage())) {
                return true;
            }
        }

        // Cause
        return isADisconnectionException(t.getCause());
    }

    private SocketTools() {
    }
}
