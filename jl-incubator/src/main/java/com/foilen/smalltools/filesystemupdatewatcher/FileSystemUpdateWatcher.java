/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.filesystemupdatewatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;

/**
 * This class is looking at any changes made to any file in the directory and their sub-directory if needed.
 * 
 * On initialization, it creates a separate thread that will call the observers when new events are ready.
 * 
 * Default:
 * <ul>
 * <li>recursive = false</li>
 * </ul>
 */
public class FileSystemUpdateWatcher extends Thread {

    // Options
    private Path basePath;
    private boolean recursive = false;

    // Internals
    private boolean initialized = false;
    private WatchService fsWatchService;
    private List<FileSystemUpdateHandler> fileSystemUpdateHandlers = new ArrayList<>();

    private Map<WatchKey, Path> pathByKey = new HashMap<WatchKey, Path>();

    public FileSystemUpdateWatcher(File basePath) {
        this.basePath = basePath.toPath();
    }

    public FileSystemUpdateWatcher(Path basePath) {
        this.basePath = basePath;
    }

    public FileSystemUpdateWatcher(String basePath) {
        this.basePath = Paths.get(basePath);
    }

    /**
     * Add an handler.
     * 
     * @param fileSystemUpdateHandler
     *            the handler
     * @return this
     */
    public FileSystemUpdateWatcher addHandler(FileSystemUpdateHandler fileSystemUpdateHandler) {
        fileSystemUpdateHandlers.add(fileSystemUpdateHandler);
        return this;
    }

    /**
     * Call after setting this object to make it work.
     * 
     * @return this
     */
    public FileSystemUpdateWatcher init() {

        AssertTools.assertFalse(initialized, "Already initialized");
        AssertTools.assertFalse(fileSystemUpdateHandlers.isEmpty(), "There are no handlers");

        // Register the directories
        try {
            fsWatchService = FileSystems.getDefault().newWatchService();
            registerRecursively(basePath);
        } catch (IOException e) {
            throw new SmallToolsException(e);
        }

        // Start the watch thread
        start();

        initialized = true;

        return this;
    }

    /**
     * Tells if the watch is recursive.
     * 
     * @return recursive
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Register a new path to check.
     * 
     * @param path
     *            the path
     */
    protected void register(Path path) {
        try {
            WatchKey key = path.register(fsWatchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            pathByKey.put(key, path);
        } catch (IOException e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Register a new path to check and all its children if it is set recursive.
     * 
     * @param path
     *            the path
     */
    private void registerRecursively(Path path) {

        register(path);

        File file = path.toFile();
        if (recursive && file.isDirectory()) {
            // Go through sub-folders
            for (File sub : file.listFiles()) {
                if (sub.isDirectory()) {
                    registerRecursively(sub.toPath());
                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        for (;;) {

            // Wait for the next event
            WatchKey key;
            try {
                key = fsWatchService.take();
            } catch (InterruptedException e) {
                throw new SmallToolsException(e);
            }

            // Go through all the events
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;

                // Check the path
                Path completePath = pathByKey.get(key).resolve(pathEvent.context());
                File completeFile = completePath.toFile();

                // Check the event
                WatchEvent.Kind<Path> kind = pathEvent.kind();
                if (StandardWatchEventKinds.OVERFLOW.equals(kind)) {
                    continue;
                } else if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                    // Check if is a directory and we want to register it
                    if (recursive && Files.isDirectory(completePath, LinkOption.NOFOLLOW_LINKS)) {
                        registerRecursively(completePath);
                    }

                    for (FileSystemUpdateHandler handler : fileSystemUpdateHandlers) {
                        handler.created(completeFile);
                    }
                } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                    for (FileSystemUpdateHandler handler : fileSystemUpdateHandlers) {
                        handler.modified(completeFile);
                    }
                } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                    for (FileSystemUpdateHandler handler : fileSystemUpdateHandlers) {
                        handler.deleted(completeFile);
                    }
                }

            }

            // Reset
            if (!key.reset()) {
                pathByKey.remove(key);
            }

        }
    }

    /**
     * Change the recursive parameter.
     * 
     * @param recursive
     *            true to watch the subfolders as well
     * @return this
     */
    public FileSystemUpdateWatcher setRecursive(boolean recursive) {

        AssertTools.assertFalse(initialized, "Cannot set recursive when already initialized");

        this.recursive = recursive;

        return this;
    }

}
