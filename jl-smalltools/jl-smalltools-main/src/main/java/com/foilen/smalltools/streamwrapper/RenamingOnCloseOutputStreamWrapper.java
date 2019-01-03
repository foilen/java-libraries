/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.FileTools;

/**
 * When closing this stream, it will rename the specified file. Mostly used to have a staging file that will get its final name when finished to write to it (e.g. downloading file).
 *
 * Extra features:
 * <ul>
 * <li>While processing, you can choose to change the state to "delete on close". You should do that if you got and incomplete stream and you want to discard it on close</li>
 * <li>You can also chose to "delete on close" per default and confirm to "rename on close" when you know the processing is correct.
 * </ul>
 *
 * You can use it via {@link FileTools#createStagingFile(File, File)} and {@link FileTools#createStagingFile(String, String)}.
 */
public class RenamingOnCloseOutputStreamWrapper extends AbstractOutputStreamWrapper {

    private static final Logger logger = LoggerFactory.getLogger(RenamingOnCloseOutputStreamWrapper.class);

    private File renameSourceFile;
    private File renameDestinationFile;

    private boolean deleteOnClose = false;

    private boolean wasClosed = false;

    /**
     * Create the wrapped output stream that will rename on close.
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

    /**
     * Create the wrapped output stream that will rename or delete on close (you can change the desired outcome during processing).
     *
     * @param wrappedOutputStream
     *            the output stream to wait for close
     * @param renameSourceFile
     *            the file that will be renamed when the stream is closed
     * @param renameDestinationFile
     *            the name to rename to
     * @param deleteOnClose
     *            will delete the file instead of renaming it (good to confirm full write)
     */
    public RenamingOnCloseOutputStreamWrapper(OutputStream wrappedOutputStream, File renameSourceFile, File renameDestinationFile, boolean deleteOnClose) {
        super(wrappedOutputStream);
        this.renameSourceFile = renameSourceFile;
        this.renameDestinationFile = renameDestinationFile;
        this.deleteOnClose = deleteOnClose;
    }

    @Override
    public void close() throws IOException {
        if (wasClosed) {
            return;
        }
        wasClosed = true;
        super.close();
        try {

            if (deleteOnClose) {
                logger.debug("Discarding the file ", renameSourceFile.getAbsolutePath());
                renameSourceFile.delete();
            } else {
                logger.debug("Renaming the file {} -> {}", renameSourceFile.getAbsolutePath(), renameDestinationFile.getAbsolutePath());
                renameDestinationFile.delete();
                if (!renameSourceFile.renameTo(renameDestinationFile)) {
                    throw new SmallToolsException("Could not rename the file [" + renameSourceFile.getAbsolutePath() + "] to [" + renameDestinationFile.getAbsolutePath() + "]");
                }
            }
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Problem renaming the file", e);
        }
    }

    /**
     * Tells if the file will be discarded or renamed when closing the stream.
     *
     * @return true if the file will be discarded ; false if will be renamed
     */
    public boolean isDeleteOnClose() {
        return deleteOnClose;
    }

    /**
     * Tells if the file will be discarded or renamed when closing the stream.
     *
     * @param deleteOnClose
     *            true if the file will be discarded ; false if will be renamed
     */
    public void setDeleteOnClose(boolean deleteOnClose) {
        this.deleteOnClose = deleteOnClose;
    }

}
