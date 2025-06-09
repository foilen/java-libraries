package com.foilen.smalltools.upgrader.trackers;

import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A tracker that stores the successfully executed tasks in a {@link Set} and stores it in a file.
 */
public class JsonFileUpgraderTracker implements UpgraderTracker {

    private final String fileName;

    private Set<String> successfulTasks = new HashSet<>();

    /**
     * The file name to use.
     *
     * @param fileName the file name
     */
    public JsonFileUpgraderTracker(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void executionBegin(String taskSimpleName) {
    }

    @Override
    public void executionEnd(String taskSimpleName, boolean isSuccessful) {
        if (isSuccessful) {
            successfulTasks.add(taskSimpleName);
            save();
        }
    }

    private void save() {
        String tmpFile = fileName + ".tmp";
        try (var staging = FileTools.createStagingFile(tmpFile, fileName, true)) {
            JsonTools.writeToStream(staging, successfulTasks);
            staging.setDeleteOnClose(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void trackerBegin() {
        if (!FileTools.exists(fileName)) {
            save();
        }
        successfulTasks = JsonTools.readFromFile(fileName, Set.class);
    }

    @Override
    public void trackerEnd() {
        save();
    }

    @Override
    public boolean wasExecutedSuccessfully(String taskSimpleName) {
        return successfulTasks.contains(taskSimpleName);
    }

}
