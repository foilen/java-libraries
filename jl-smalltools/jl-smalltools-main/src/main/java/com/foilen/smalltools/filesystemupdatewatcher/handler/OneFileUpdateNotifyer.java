package com.foilen.smalltools.filesystemupdatewatcher.handler;

import com.foilen.smalltools.filesystemupdatewatcher.FileSystemUpdateBufferedHandler;
import com.foilen.smalltools.filesystemupdatewatcher.FileSystemUpdateHandler;
import com.foilen.smalltools.filesystemupdatewatcher.FileSystemUpdateWatcher;
import com.foilen.smalltools.tools.CloseableTools;

import java.io.Closeable;
import java.io.File;

/**
 * Use this class to track a file modification (e.g a config file that you want to reload when changed). If the file changes multiple times quickly, you will get a notification 2 seconds after the
 * last change or max 10 seconds after the first change.
 * <p>
 * Don't forget to call {@link #initAutoUpdateSystem()} when you are ready to get the notifications
 */
public class OneFileUpdateNotifyer implements Closeable, FileSystemUpdateHandler {

    private String fileToWatch;
    private File fileToWatchFile;
    private OneFileUpdateNotifyerHandler handler;
    private FileSystemUpdateWatcher fileSystemUpdateWatcher;

    /**
     * @param fileToWatch the file to watch
     * @param handler     the handler to call when the file is updated
     */
    public OneFileUpdateNotifyer(String fileToWatch, OneFileUpdateNotifyerHandler handler) {
        this.fileToWatch = fileToWatch;
        fileToWatchFile = new File(fileToWatch);
        this.handler = handler;
    }

    @Override
    public void close() {
        CloseableTools.close(fileSystemUpdateWatcher);
    }

    @Override
    public void created(File file) {
        if (file.equals(fileToWatchFile)) {
            handler.fileUpdated(fileToWatch);
        }
    }

    @Override
    public void deleted(File file) {
        if (file.equals(fileToWatchFile)) {
            handler.fileUpdated(fileToWatch);
        }
    }

    /**
     * Sends the first update event and start monitoring.
     */
    public void initAutoUpdateSystem() {
        handler.fileUpdated(fileToWatch);
        fileSystemUpdateWatcher = new FileSystemUpdateWatcher(fileToWatchFile.getParentFile().getAbsolutePath());
        fileSystemUpdateWatcher.addHandler(new FileSystemUpdateBufferedHandler(this, 2000, 10000));
        fileSystemUpdateWatcher.init();
    }

    @Override
    public void modified(File file) {
        if (file.equals(fileToWatchFile)) {
            handler.fileUpdated(fileToWatch);
        }
    }

}
