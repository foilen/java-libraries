/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import com.foilen.smalltools.upgrader.UpgraderTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;

/**
 * An upgrade tracker to record which {@link UpgradeTask} was executed by {@link UpgraderTools}.
 */
public interface UpgraderTracker {

    /**
     * Called before each task.
     *
     * @param taskSimpleName
     *            the task simple class name
     */
    void executionBegin(String taskSimpleName);

    /**
     * Called after each task.
     *
     * @param taskSimpleName
     *            the task simple class name
     * @param isSuccessful
     *            true if was successful
     */
    void executionEnd(String taskSimpleName, boolean isSuccessful);

    /**
     * Called before any task is executed to prepare the tracker if needed.
     */
    void trackerBegin();

    /**
     * Called at the end to cleanup the tracker if needed.
     */
    void trackerEnd();

    /**
     * Tells if a task was already executed successfully.
     *
     * @param taskSimpleName
     *            the task simple class name
     * @return true if was executed
     */
    boolean wasExecutedSuccessfully(String taskSimpleName);

}
