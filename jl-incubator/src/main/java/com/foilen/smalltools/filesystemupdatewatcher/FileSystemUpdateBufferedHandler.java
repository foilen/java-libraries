/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.filesystemupdatewatcher;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.foilen.smalltools.tools.ThreadTools;

/**
 * This is the handler that will get the notifications.
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

    private FileSystemUpdateHandler wrappedHandler;

    private static FileState[] matrixExisted = { FileState.MODIFIED, FileState.MODIFIED, FileState.DELETED };
    private static FileState[] matrixNotExisted = { FileState.CREATED, FileState.CREATED, null };

    // Parameters
    private long maxDelayMs;
    private long delayAfterLastEventMs;

    // Running state
    private Object lock = new Object();
    private Map<File, FileStatus> buffer = new HashMap<>();
    private long firstEventTimeMs = -1;
    private long lastEventTimeMs;
    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {

            // Never stop checking
            for (;;) {

                // Wait for at least the first event
                if (firstEventTimeMs == -1) {
                    ThreadTools.sleep(delayAfterLastEventMs);
                    continue;
                }

                long maxTime = firstEventTimeMs + maxDelayMs;
                for (;;) {
                    // Wait if not reached
                    long now = new Date().getTime();
                    long waitTime = Math.min( //
                            lastEventTimeMs + delayAfterLastEventMs - now, //
                            maxTime - now);

                    if (waitTime <= 0) {
                        break;
                    }

                    ThreadTools.sleep(waitTime);
                }

                // Process
                Map<File, FileStatus> toProcess;
                synchronized (lock) {
                    firstEventTimeMs = -1;
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
            }
        }
    }, "FileSystemUpdateBufferedHandler");

    public FileSystemUpdateBufferedHandler(FileSystemUpdateHandler wrappedHandler, long delayAfterLastEventMs, long maxDelayMs) {
        this.wrappedHandler = wrappedHandler;
        this.delayAfterLastEventMs = delayAfterLastEventMs;
        this.maxDelayMs = maxDelayMs;

        // Prepare the thread
        thread.setDaemon(true);
        thread.start();
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

            // Reset the lastEventTime
            lastEventTimeMs = new Date().getTime();

            // Set the first event time if not started
            if (firstEventTimeMs == -1) {
                firstEventTimeMs = new Date().getTime();
            }
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