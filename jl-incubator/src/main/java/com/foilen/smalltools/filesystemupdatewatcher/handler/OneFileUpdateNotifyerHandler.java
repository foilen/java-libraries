/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.filesystemupdatewatcher.handler;

public interface OneFileUpdateNotifyerHandler {

    /**
     * Implement this method that reloads the file when changed.
     * 
     * IMPORTANT: it must not throw any exception (e.g: consider no file as an empty file)
     * 
     * @param fileName
     *            the full path of the file
     */
    void fileUpdated(String fileName);

}
