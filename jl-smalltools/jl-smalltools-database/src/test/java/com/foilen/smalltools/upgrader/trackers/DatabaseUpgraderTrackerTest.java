/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.trackers;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

public class DatabaseUpgraderTrackerTest extends AbstractUpgraderTrackerTest {

    public DatabaseUpgraderTrackerTest() {

        EmbeddedDatabaseBuilder databaseBuilder = new EmbeddedDatabaseBuilder();
        databaseBuilder.setType(EmbeddedDatabaseType.H2);

        DataSource dataSource = databaseBuilder.build();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        init(new DatabaseUpgraderTracker(jdbcTemplate));
    }

}
