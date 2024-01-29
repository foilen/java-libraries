/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.tasks;

import com.foilen.smalltools.tools.ResourceTools;
import com.google.common.base.Strings;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Use this to have some common database helpers for your task.
 */
public abstract class AbstractDatabaseUpgradeTask extends AbstractUpgradeTask {

    /**
     * The jdbc template to use.
     */
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Find all the tables in the database.
     *
     * @return the list of tables
     */
    protected List<String> mysqlTablesFindAll() {
        List<String> tableNames = jdbcTemplate.queryForList("SHOW TABLES", String.class);
        Collections.sort(tableNames);
        return tableNames;
    }

    /**
     * Purge the connections in the pool.
     */
    protected void purgeConnections() {
        javax.sql.DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource instanceof DataSource) {
            DataSource tomcatDatasource = (DataSource) dataSource;
            tomcatDatasource.purge();
        } else {
            logger.error("Cannot purge the connections. Only supports the Tomcat JDBC pool");
        }
    }

    /**
     * Take a resource file and execute all the queries in it.
     *
     * @param resourceName the name of the resource relative to this class
     */
    protected void updateFromResource(String resourceName) {
        updateFromResource(resourceName, this.getClass());
    }

    /**
     * Take a resource file and execute all the queries in it.
     *
     * @param resourceName the name of the resource relative to the context class
     * @param resourceCtx  the context class
     */
    protected void updateFromResource(String resourceName, Class<?> resourceCtx) {
        String fullSql = ResourceTools.getResourceAsString(resourceName, resourceCtx);
        for (String sql : fullSql.split(";")) {
            sql = sql.trim();
            if (!Strings.isNullOrEmpty(sql)) {
                jdbcTemplate.update(sql);
            }
        }
    }

    /**
     * Tells the tracker name to use.
     *
     * @return the name
     */
    @Override
    public String useTracker() {
        return "db";
    }

}
