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
