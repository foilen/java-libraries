/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.net.SocketException;

import com.foilen.smalltools.exception.EndOfStreamException;

/**
 * Some simple methods to play with sockets.
 */
public final class SocketTools {

    /**
     * Tells if the throwable or any cause of it is a disconnection from a socket.
     *
     * @param t the throwable
     * @return true if is a disconnection from a socket
     */
    public static boolean isADisconnectionException(Throwable t) {

        if (t == null) {
            return false;
        }

        // Current
        if (t instanceof SocketException) {
            if ("Connection reset".equals(t.getMessage())) {
                return true;
            }
            if ("Socket closed".equals(t.getMessage())) {
                return true;
            }
            if ("socket closed".equals(t.getMessage())) {
                return true;
            }
        }
        if (t instanceof EndOfStreamException) {
            return true;
        }

        // Cause
        return isADisconnectionException(t.getCause());
    }

    private SocketTools() {
    }
}
