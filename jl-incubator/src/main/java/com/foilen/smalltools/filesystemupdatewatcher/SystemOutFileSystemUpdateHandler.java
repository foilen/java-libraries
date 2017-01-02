/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.filesystemupdatewatcher;

import java.io.File;

/**
 * Show the changes to the standard output.
 */
public class SystemOutFileSystemUpdateHandler implements FileSystemUpdateHandler {

    @Override
    public void created(File file) {
        System.out.println("CREATED: " + file.getAbsolutePath());
    }

    @Override
    public void deleted(File file) {
        System.out.println("DELETED: " + file.getAbsolutePath());
    }

    @Override
    public void modified(File file) {
        System.out.println("MODIFIED: " + file.getAbsolutePath());
    }

}
