/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.AntPathMatcher;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.upgrader.UpgraderTools;

/**
 * Extend this class to have the right task name. It will take all the files in /db/migration/*.sql and execute those not already successfully passed.
 *
 * This is not a replacement for Flyway; just an easy way to migrate off of it to use only the {@link UpgraderTools} after that.
 *
 * By default, it will delete the "schema_version" table at the end.
 */
public abstract class AbstractFlywayMigrateOffUpgradeTask extends AbstractDatabaseUpgradeTask {

    private boolean deleteSchemaTable = true;

    @Override
    public void execute() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(jdbcTemplate.getDataSource());

        // Check if the schema_version table exists
        List<String> tableNames = mysqlTablesFindAll();
        List<String> executed;
        if (tableNames.contains("schema_version")) {
            executed = jdbcTemplate.queryForList("SELECT script FROM schema_version WHERE success = 1", String.class);
            jdbcTemplate.update("DELETE FROM schema_version WHERE success = 0");
        } else {
            executed = new ArrayList<>();
            logger.info("Flyway table does not exists. Creating it");
            updateFromResource("flyway-schema_version.sql", AbstractFlywayMigrateOffUpgradeTask.class);
        }

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        resolver.setPathMatcher(pathMatcher);
        Resource[] resources;
        try {
            resources = resolver.getResources("classpath:db/migration/*.sql");
        } catch (IOException e) {
            throw new SmallToolsException("Problem getting the sql files", e);
        }

        int rank = executed.size() + 1;
        List<String> scriptNames = Arrays.asList(resources).stream() //
                .map(Resource::getFilename) //
                .sorted() //
                .collect(Collectors.toList());
        for (String scriptName : scriptNames) {
            boolean needRetry = true;
            if (executed.contains(scriptName)) {
                logger.info("[{}] Already executed. Skip", scriptName);
            } else {
                logger.info("[{}] To execute", scriptName);
                for (int retryCount = 0; needRetry; ++retryCount) {
                    needRetry = false;
                    TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
                    try {
                        // Do the update
                        updateFromResource("/db/migration/" + scriptName);

                        // Save in schema_version
                        jdbcTemplate.update("INSERT INTO schema_version " //
                                + "(version_rank, installed_rank, version, description, type, script, installed_by, execution_time, success) " //
                                + "VALUES (?,?,?,'','SQL',?,'upgrader',1, 1)", //
                                rank, rank, //
                                scriptName.substring(0, Math.min(50, scriptName.length())), //
                                scriptName //
                        );
                        ++rank;
                        transactionManager.commit(transactionStatus);
                    } catch (Exception e) {
                        logger.warn("[{}] Problem executing script. Will purge the connections and retry", scriptName);
                        transactionManager.rollback(transactionStatus);
                        needRetry = true;
                        purgeConnections();
                        if (retryCount > 5) {
                            throw new SmallToolsException("Problem executing script: " + scriptName, e);
                        }
                    }

                }
            }
        }

        if (deleteSchemaTable) {
            logger.info("Deleting the Flyway schema_version table");
            jdbcTemplate.update("DROP TABLE schema_version");
        }

    }

    public boolean isDeleteSchemaTable() {
        return deleteSchemaTable;
    }

    public void setDeleteSchemaTable(boolean deleteSchemaTable) {
        this.deleteSchemaTable = deleteSchemaTable;
    }

}
