/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.filesystemupdatewatcher;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.foilen.smalltools.trigger.SmoothTrigger;

/**
 * This is an handler that will get the notifications for a few seconds and then trigger back a summary (e.g if a file is changed 10 times in 1 second, you will be notified only once instead of 10
 * times).
 *
 * The buffering:
 * <ul>
 * <li>When an event is received, 2 timers start: the total and the one from last event.</li>
 * <li>When another event is received, the timer from last event is restarted from 0.</li>
 * <li>When the timer from last event reached delayAfterLastEventMs, the summary is fired.</li>
 * <li>If there are continuously new events, when the total timer reaches maxDelayMs, the summary is fired.</li>
 * </ul>
 *
 * To be more efficient, the summaries are not necessarily fired in the same order for different files.
 *
 * The summary about the same file:
 * <ul>
 * <li>Many times the same event = once that event</li>
 * <li>1 Created + X Modified = 1 Created</li>
 * <li>1 Created + X Modified + 1 Deleted = 0 event</li>
 * <li>X Modified + 1 Deleted = 1 Deleted</li>
 * <li>1 Deleted + 1 Created = 1 Modified</li>
 * </ul>
 *
 */
public class FileSystemUpdateBufferedHandler implements FileSystemUpdateHandler {

    private static enum FileState {
        CREATED, MODIFIED, DELETED
    }

    private static class FileStatus {
        private boolean existed;
        private FileState lastEvent;

        public FileStatus(boolean existed, FileState lastEvent) {
            this.existed = existed;
            this.lastEvent = lastEvent;
        }

    }

    private static FileState[] matrixExisted = { FileState.MODIFIED, FileState.MODIFIED, FileState.DELETED };
    private static FileState[] matrixNotExisted = { FileState.CREATED, FileState.CREATED, null };

    // Running state
    private SmoothTrigger smoothTrigger;
    private Object lock = new Object();
    private Map<File, FileStatus> buffer = new HashMap<>();

    public FileSystemUpdateBufferedHandler(FileSystemUpdateHandler wrappedHandler, long delayAfterLastEventMs, long maxDelayMs) {

        smoothTrigger = new SmoothTrigger(() -> {

            // Process
            Map<File, FileStatus> toProcess;
            synchronized (lock) {
                toProcess = buffer;
                buffer = new HashMap<>();
            }

            for (Entry<File, FileStatus> entry : toProcess.entrySet()) {
                File file = entry.getKey();
                FileStatus fileStatus = entry.getValue();

                // Compute the next
                FileState fileState;
                if (fileStatus.existed) {
                    fileState = matrixExisted[fileStatus.lastEvent.ordinal()];
                } else {
                    fileState = matrixNotExisted[fileStatus.lastEvent.ordinal()];
                }
                if (fileState == null) {
                    continue;
                }

                switch (fileState) {
                case CREATED:
                    wrappedHandler.created(file);
                    break;
                case DELETED:
                    wrappedHandler.deleted(file);
                    break;
                case MODIFIED:
                    wrappedHandler.modified(file);
                    break;
                default:
                    break;
                }
            }

        }) //
                .setDelayAfterLastTriggerMs(delayAfterLastEventMs) //
                .setMaxDelayAfterFirstRequestMs(maxDelayMs) //
                .start();

    }

    private void bufferEvent(File file, boolean existed, FileState lastEvent) {

        synchronized (lock) {

            // Get or create the state
            FileStatus fileStatus = buffer.get(file);
            if (fileStatus == null) {
                fileStatus = new FileStatus(existed, lastEvent);
                buffer.put(file, fileStatus);
            }

            // Set the lastEvent
            fileStatus.lastEvent = lastEvent;

            // Tell to eventually process
            smoothTrigger.request();
        }

    }

    @Override
    public void created(File file) {
        bufferEvent(file, false, FileState.CREATED);
    }

    @Override
    public void deleted(File file) {
        bufferEvent(file, true, FileState.DELETED);

    }

    @Override
    public void modified(File file) {
        bufferEvent(file, true, FileState.MODIFIED);
    }
}