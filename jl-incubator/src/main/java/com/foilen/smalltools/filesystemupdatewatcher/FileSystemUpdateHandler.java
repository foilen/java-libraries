/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.filesystemupdatewatcher;

import java.io.File;

/**
 * This is the handler that will get the notifications.
 */
public interface FileSystemUpdateHandler {

    /**
     * Called when a file or directory is created.
     * 
     * @param file
     */
    void created(File file);

    /**
     * Called when a file or directory is deleted.
     * 
     * @param file
     */
    void deleted(File file);

    /**
     * Called when a file or directory is modified.
     * 
     * @param file
     */
    void modified(File file);
}