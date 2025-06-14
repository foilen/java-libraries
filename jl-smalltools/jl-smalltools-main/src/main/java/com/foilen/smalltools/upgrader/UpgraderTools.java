package com.foilen.smalltools.upgrader;

import com.foilen.smalltools.comparator.ClassSimpleNameComparator;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.foilen.smalltools.upgrader.trackers.UpgraderTracker;
import jakarta.annotation.PostConstruct;

import java.util.*;

/**
 * A tool to help manage upgrade tasks. (E.g: database, file updates, ...)
 * <p>
 * You can choose different backends to keep track of what was already executed.
 * <p>
 * The execution of the tasks will be by their simple classname so that you can move them around packages.
 */
public class UpgraderTools extends AbstractBasics {

    private UpgraderTracker defaultUpgraderTracker;
    private Map<String, UpgraderTracker> upgraderTrackerByName = new HashMap<>();
    private List<UpgradeTask> tasks = new ArrayList<>();

    private boolean sortByClassName = true;

    /**
     * Create an empty instance. You will need to add the tasks and trackers later.
     */
    public UpgraderTools() {
    }

    /**
     * Create an instance with the tasks.
     *
     * @param tasks the tasks
     */
    public UpgraderTools(List<UpgradeTask> tasks) {
        this.tasks = tasks;
    }

    /**
     * Add a tracker.
     *
     * @param trackerName     the name of the tracker
     * @param upgraderTracker the tracker
     * @return this
     */
    public UpgraderTools addUpgraderTracker(String trackerName, UpgraderTracker upgraderTracker) {
        upgraderTrackerByName.put(trackerName, upgraderTracker);
        return this;
    }

    /**
     * Execute all the tasks.
     *
     * @throws Exception if any problem
     */
    @PostConstruct
    public void execute() throws Exception {
        logger.info("Starting upgrades");

        if (sortByClassName) {
            Collections.sort(tasks, new ClassSimpleNameComparator());
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
            } catch (Throwable e) {
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

    /**
     * Get the default tracker.
     *
     * @return the default tracker
     */
    public UpgraderTracker getDefaultUpgraderTracker() {
        return defaultUpgraderTracker;
    }

    /**
     * Set the default tracker.
     *
     * @param defaultUpgraderTracker the default tracker
     */
    public void setDefaultUpgraderTracker(UpgraderTracker defaultUpgraderTracker) {
        this.defaultUpgraderTracker = defaultUpgraderTracker;
    }

    /**
     * Get the tasks.
     *
     * @return the tasks
     */
    public List<UpgradeTask> getTasks() {
        return tasks;
    }

    /**
     * Set the tasks.
     *
     * @param tasks the tasks
     */
    public void setTasks(List<UpgradeTask> tasks) {
        this.tasks = tasks;
    }

    /**
     * Get the trackers by name.
     *
     * @return the trackers by name
     */
    public Map<String, UpgraderTracker> getUpgraderTrackerByName() {
        return upgraderTrackerByName;
    }

    /**
     * Set the trackers by name.
     *
     * @param upgraderTrackerByName the trackers by name
     */
    public void setUpgraderTrackerByName(Map<String, UpgraderTracker> upgraderTrackerByName) {
        this.upgraderTrackerByName = upgraderTrackerByName;
    }

    /**
     * If the tasks should be sorted by class name.
     *
     * @return true if sorted
     */
    public boolean isSortByClassName() {
        return sortByClassName;
    }

    /**
     * If the tasks should be sorted by class name.
     *
     * @param sortByClassName true if sorted
     */
    public void setSortByClassName(boolean sortByClassName) {
        this.sortByClassName = sortByClassName;
    }

}
