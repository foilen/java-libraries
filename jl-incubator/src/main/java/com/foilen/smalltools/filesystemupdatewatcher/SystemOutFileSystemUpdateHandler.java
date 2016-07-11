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
