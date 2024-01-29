/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * A tracker that stores the successfully executed tasks in a database.
 */
public class DatabaseUpgraderTracker implements UpgraderTracker {

    private JdbcTemplate jdbcTemplate;

    /**
     * The constructor.
     *
     * @param jdbcTemplate the jdbcTemplate
     */
    public DatabaseUpgraderTracker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * What to do when a task starts.
     *
     * @param taskSimpleName the task simple class name
     */
    @Override
    public void executionBegin(String taskSimpleName) {
    }

    /**
     * What to do when a task ends.
     *
     * @param taskSimpleName the task simple class name
     * @param isSuccessful   if the task was successful
     */
    @Override
    public void executionEnd(String taskSimpleName, boolean isSuccessful) {
        if (isSuccessful) {
            jdbcTemplate.update("INSERT INTO upgrader_tools (task) VALUES (?)", taskSimpleName);
        }
    }

    /**
     * What to do when the tracker starts.
     */
    @Override
    public void trackerBegin() {
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS upgrader_tools (task varchar(255) PRIMARY KEY)");
    }

    /**
     * What to do when the tracker ends.
     */
    @Override
    public void trackerEnd() {
    }

    /**
     * Check if a task was executed successfully.
     *
     * @param taskSimpleName the task simple class name
     * @return true if it was executed successfully
     */
    @Override
    public boolean wasExecutedSuccessfully(String taskSimpleName) {
        List<String> tasks = jdbcTemplate.queryForList("SELECT task FROM upgrader_tools WHERE task = ?", String.class, taskSimpleName);
        return !tasks.isEmpty();
    }

}
