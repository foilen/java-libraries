/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A tracker that stores the successfully executed tasks in a database.
 */
public class DatabaseUpgraderTracker implements UpgraderTracker {

    private JdbcTemplate jdbcTemplate;

    public DatabaseUpgraderTracker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void executionBegin(String taskSimpleName) {
    }

    @Override
    public void executionEnd(String taskSimpleName, boolean isSuccessful) {
        if (isSuccessful) {
            jdbcTemplate.update("INSERT INTO upgrader_tools (task) VALUES (?)", taskSimpleName);
        }
    }

    @Override
    public void trackerBegin() {
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS upgrader_tools (task varchar(255) PRIMARY KEY)");
    }

    @Override
    public void trackerEnd() {
    }

    @Override
    public boolean wasExecutedSuccessfully(String taskSimpleName) {
        List<String> tasks = jdbcTemplate.queryForList("SELECT task FROM upgrader_tools WHERE task = ?", String.class, taskSimpleName);
        return !tasks.isEmpty();
    }

}
