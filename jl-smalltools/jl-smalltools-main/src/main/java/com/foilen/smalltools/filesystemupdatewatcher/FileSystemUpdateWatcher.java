package com.foilen.smalltools.filesystemupdatewatcher;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.ThreadTools;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is looking at any changes made to any file in the directory and their sub-directory if needed.
 * <p>
 * On initialization, it creates a separate thread that will call the observers when new events are ready.
 * <p>
 * Default:
 * <ul>
 * <li>recursive = false</li>
 * </ul>
 * <p>
 * Usage:
 *
 * <pre>
 * // Start
 * FileSystemUpdateWatcher watcher = new FileSystemUpdateWatcher("/");
 * watcher.addHandler(new SystemOutFileSystemUpdateHandler());
 * watcher.init();
 *
 * ThreadTools.sleep(2000);
 *
 * // Stop
 * watcher.close();
 * ThreadTools.sleep(2000);
 *
 * // Start and again
 * watcher.init();
 * ThreadTools.sleep(2000);
 * watcher.close();
 * </pre>
 */
public class FileSystemUpdateWatcher implements Closeable {

    /**
     * Main for testing.
     *
     * @param args ignored
     */
    public static void main(String[] args) {

        // Start
        FileSystemUpdateWatcher watcher = new FileSystemUpdateWatcher("/");
        watcher.addHandler(new SystemOutFileSystemUpdateHandler());
        watcher.init();

        ThreadTools.sleep(2000);

        // Stop
        watcher.close();
        ThreadTools.sleep(2000);

        // Start and again
        watcher.init();
        ThreadTools.sleep(2000);
        watcher.close();
    }

    // Options
    private Path basePath;

    private boolean recursive = false;
    // Internals
    private Thread thread;
    private WatchService fsWatchService;

    private List<FileSystemUpdateHandler> fileSystemUpdateHandlers = new ArrayList<>();

    private Map<WatchKey, Path> pathByKey = new HashMap<WatchKey, Path>();

    /**
     * Register the directory.
     *
     * @param basePath the directory
     */
    public FileSystemUpdateWatcher(File basePath) {
        this.basePath = basePath.toPath();
    }

    /**
     * Register the directory.
     *
     * @param basePath the directory
     */
    public FileSystemUpdateWatcher(Path basePath) {
        this.basePath = basePath;
    }

    /**
     * Register the directory.
     *
     * @param basePath the directory
     */
    public FileSystemUpdateWatcher(String basePath) {
        this.basePath = Paths.get(basePath);
    }

    /**
     * Add an handler.
     *
     * @param fileSystemUpdateHandler the handler
     * @return this
     */
    public FileSystemUpdateWatcher addHandler(FileSystemUpdateHandler fileSystemUpdateHandler) {
        fileSystemUpdateHandlers.add(fileSystemUpdateHandler);
        return this;
    }

    @Override
    public void close() {
        thread = null;
        CloseableTools.close(fsWatchService);
    }

    /**
     * Call after setting this object to make it work. Stop it with {@link #close()}.
     *
     * @return this
     */
    public FileSystemUpdateWatcher init() {

        AssertTools.assertNull(thread, "Already initialized");
        AssertTools.assertFalse(fileSystemUpdateHandlers.isEmpty(), "There are no handlers");

        // Register the directories
        try {
            fsWatchService = FileSystems.getDefault().newWatchService();
            registerRecursively(basePath);
        } catch (IOException e) {
            throw new SmallToolsException(e);
        }

        // Start the watch thread
        thread = new Thread(() -> {
            ThreadTools.nameThread().setSeparator("-").clear() //
                    .appendObjectClassSimple(this) //
                    .appendText("Started at") //
                    .appendDate() //
                    .appendObjectText("Folder") //
                    .appendObjectText(basePath).change();

            for (; ; ) {

                // Wait for the next event
                WatchKey key;
                try {
                    key = fsWatchService.take();
                } catch (ClosedWatchServiceException e) {
                    break;
                } catch (InterruptedException e) {
                    throw new SmallToolsException(e);
                }

                // Go through all the events
                for (WatchEvent<?> event : key.pollEvents()) {
                    @SuppressWarnings("unchecked")
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

        });
        thread.setDaemon(true);
        thread.start();

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
     * @param path the path
     */
    protected void register(Path path) {
        File directory = path.toFile();
        AssertTools.assertTrue(directory.exists(), "The directory must exists prior to watching. Path: " + path);
        AssertTools.assertTrue(directory.isDirectory(), "The path must be a directory. Path: " + path);
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
     * @param path the path
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

    /**
     * Change the recursive parameter.
     *
     * @param recursive true to watch the subfolders as well
     * @return this
     */
    public FileSystemUpdateWatcher setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

}
