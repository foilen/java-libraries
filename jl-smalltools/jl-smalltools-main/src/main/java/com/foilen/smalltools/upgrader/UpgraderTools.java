/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.comparator.ClassNameComparator;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.foilen.smalltools.upgrader.trackers.UpgraderTracker;

/**
 * A tool to help manage upgrade tasks. (E.g: database, file updates, ...)
 *
 * You can choose different backends to keep track of what was already executed.
 * 
 * <pre>
 * Dependencies:
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 */
public class UpgraderTools {

    static private final Logger logger = LoggerFactory.getLogger(UpgraderTools.class);

    private UpgraderTracker defaultUpgraderTracker;
    private Map<String, UpgraderTracker> upgraderTrackerByName = new HashMap<>();
    private List<UpgradeTask> tasks = new ArrayList<>();

    private boolean sortByClassName = true;

    public UpgraderTools() {
    }

    public UpgraderTools(List<UpgradeTask> tasks) {
        this.tasks = tasks;
    }

    public UpgraderTools addUpgraderTracker(String trackerName, UpgraderTracker upgraderTracker) {
        upgraderTrackerByName.put(trackerName, upgraderTracker);
        return this;
    }

    @PostConstruct
    public void execute() throws Exception {
        logger.info("Starting upgrades");

        if (sortByClassName) {
            Collections.sort(tasks, new ClassNameComparator());
        }

        // Begin all trackers
        List<UpgraderTracker> allTrackers = new ArrayList<>();
        if (defaultUpgraderTracker != null) {
            allTrackers.add(defaultUpgraderTracker);
        }
        allTrackers.addAll(upgraderTrackerByName.values());
        AssertTools.assertFalse(allTrackers.isEmpty(), "There are no upgrade tracker set");

        allTrackers.forEach(it -> {
            it.trackerBegin();
        });

        int count = 1;
        Exception failure = null;
        for (UpgradeTask task : tasks) {

            // Get the tracker
            UpgraderTracker upgraderTracker;
            String trackerName = task.useTracker();
            if (UpgradeTask.DEFAULT_TRACKER.equals(trackerName)) {
                upgraderTracker = defaultUpgraderTracker;
                AssertTools.assertNotNull(upgraderTracker, "There is no default upgrade tracker set");
            } else {
                upgraderTracker = upgraderTrackerByName.get(trackerName);
                AssertTools.assertNotNull(upgraderTracker, "There is no upgrade tracker with name: " + trackerName);
            }

            String taskSimpleName = task.getClass().getSimpleName();
            if (upgraderTracker.wasExecutedSuccessfully(taskSimpleName)) {
                logger.info("[{}/{}] Skipping {} . Already executed", count, tasks.size(), taskSimpleName);
                ++count;
                continue;
            }

            logger.info("[{}/{}] Begin {}", count, tasks.size(), taskSimpleName);
            upgraderTracker.executionBegin(taskSimpleName);
            try {
                task.execute();
            } catch (Exception e) {
                logger.error("[{}/{}] Problem executing upgrade task {}", count, tasks.size(), task.getClass().getSimpleName(), e);
                failure = new SmallToolsException("Problem executing upgrade task " + task.getClass().getSimpleName(), e);
                break;
            } finally {
                upgraderTracker.executionEnd(taskSimpleName, failure == null);
            }

            logger.info("[{}/{}] Completed {}", count, tasks.size(), task.getClass().getSimpleName());
            ++count;

        }

        // End all trackers
        allTrackers.forEach(it -> {
            it.trackerEnd();
        });

        // Throw the failure if any
        if (failure != null) {
            throw failure;
        }

        logger.info("Upgrades completed");
    }

    public UpgraderTracker getDefaultUpgraderTracker() {
        return defaultUpgraderTracker;
    }

    public List<UpgradeTask> getTasks() {
        return tasks;
    }

    public Map<String, UpgraderTracker> getUpgraderTrackerByName() {
        return upgraderTrackerByName;
    }

    public boolean isSortByClassName() {
        return sortByClassName;
    }

    public void setDefaultUpgraderTracker(UpgraderTracker defaultUpgraderTracker) {
        this.defaultUpgraderTracker = defaultUpgraderTracker;
    }

    public void setSortByClassName(boolean sortByClassName) {
        this.sortByClassName = sortByClassName;
    }

    public void setTasks(List<UpgradeTask> tasks) {
        this.tasks = tasks;
    }

    public void setUpgraderTrackerByName(Map<String, UpgraderTracker> upgraderTrackerByName) {
        this.upgraderTrackerByName = upgraderTrackerByName;
    }

}
