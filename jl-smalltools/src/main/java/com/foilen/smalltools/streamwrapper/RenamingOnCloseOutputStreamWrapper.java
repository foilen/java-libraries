/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streamwrapper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.FileTools;

/**
 * When closing this stream, it will rename the specified file. Mostly used to have a staging file that will get its final name when finished to write to it (e.g. downloading file).
 * 
 * You can use it via {@link FileTools#createStagingFile(File, File)} and {@link FileTools#createStagingFile(String, String)}.
 */
public class RenamingOnCloseOutputStreamWrapper extends AbstractOutputStreamWrapper {

    private File renameSourceFile;
    private File renameDestinationFile;

    /**
     * Create the wrapped output stream.
     * 
     * @param wrappedOutputStream
     *            the output stream to wait for close
     * @param renameSourceFile
     *            the file that will be renamed when the stream is closed
     * @param renameDestinationFile
     *            the name to rename to
     */
    public RenamingOnCloseOutputStreamWrapper(OutputStream wrappedOutputStream, File renameSourceFile, File renameDestinationFile) {
        super(wrappedOutputStream);
        this.renameSourceFile = renameSourceFile;
        this.renameDestinationFile = renameDestinationFile;
    }

    @Override
    public void close() throws IOException {
        super.close();
        try {
            renameSourceFile.renameTo(renameDestinationFile);
        } catch (Exception e) {
            throw new SmallToolsException("Problem renaming the file", e);
        }
    }

}
