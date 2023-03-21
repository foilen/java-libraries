/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.tasks;

/**
 * A task to execute.
 */
public interface UpgradeTask {

    /**
     * The default tracker to use.
     */
    public static final String DEFAULT_TRACKER = "____";

    /**
     * Execute the task. Throw a {@link RuntimeException} for any failure.
     */
    void execute();

    /**
     * Tells the name of the tracker to use. Can be {@link UpgradeTask#DEFAULT_TRACKER}.
     *
     * @return the tracker to use
     */
    String useTracker();

}
