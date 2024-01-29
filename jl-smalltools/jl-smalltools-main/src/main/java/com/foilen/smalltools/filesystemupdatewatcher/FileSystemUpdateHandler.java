/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.filesystemupdatewatcher;

import java.io.File;

/**
 * This is the handler that will get the notifications. If you want to have a summary of the changes during a certain delay (without duplicates), wrap it with {@link FileSystemUpdateBufferedHandler}.
 */
public interface FileSystemUpdateHandler {

    /**
     * Called when a file or directory is created.
     *
     * @param file the file or directory
     */
    void created(File file);

    /**
     * Called when a file or directory is deleted.
     *
     * @param file the file or directory
     */
    void deleted(File file);

    /**
     * Called when a file or directory is modified.
     *
     * @param file the file or directory
     */
    void modified(File file);
}