/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;

/**
 * A tracker that stores the successfully executed tasks in a {@link Set} and stores it in a file.
 */
public class JsonFileUpgraderTracker implements UpgraderTracker {

    private String fileName;

    private Set<String> successfulTasks = new HashSet<>();

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
        JsonTools.writeToFile(tmpFile, successfulTasks);
        AssertTools.assertTrue(new File(tmpFile).renameTo(new File(fileName)), "Could not move the temporary file");
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
