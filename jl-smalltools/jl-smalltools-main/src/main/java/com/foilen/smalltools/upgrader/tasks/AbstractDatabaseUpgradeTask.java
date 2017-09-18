/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.tasks;

import java.util.Collections;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.foilen.smalltools.tools.ResourceTools;
import com.google.common.base.Strings;

/**
 *
 * Use this to have some common database helpers for your task.
 *
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:23.0'
 * compile 'org.apache.tomcat:tomcat-jdbc:8.5.20'
 * compile 'org.springframework:spring-beans:4.3.11.RELEASE'
 * compile 'org.springframework:spring-orm:4.3.11.RELEASE'
 * </pre>
 */
public abstract class AbstractDatabaseUpgradeTask extends AbstractUpgradeTask {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected List<String> mysqlTablesFindAll() {
        List<String> tableNames = jdbcTemplate.queryForList("SHOW TABLES", String.class);
        Collections.sort(tableNames);
        return tableNames;
    }

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
     * @param resourceName
     *            the name of the resource relative to this class
     */
    protected void updateFromResource(String resourceName) {
        updateFromResource(resourceName, this.getClass());
    }

    /**
     * Take a resource file and execute all the queries in it.
     *
     * @param resourceName
     *            the name of the resource relative to the context class
     * @param resourceCtx
     *            the context class
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

    @Override
    public String useTracker() {
        return "db";
    }

}
