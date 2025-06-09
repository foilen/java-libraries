package com.foilen.smalltools.tools;

import com.foilen.smalltools.test.asserts.AssertTools;
import org.hibernate.dialect.MySQLDialect;
import org.junit.Test;

import java.io.File;

public class Hibernate63ToolsTest {

    @Test
    public void testGenerateSqlSchema_WithoutUnderscore() throws Exception {
        System.setProperty("hibernate.dialect.storage_engine", "innodb");
        String outputSqlFile = File.createTempFile("output", ".sql").getAbsolutePath();
        Hibernate63Tools.generateSqlSchema(MySQLDialect.class, outputSqlFile, false, "com.foilen.smalltools.tools.test.entities");

        String expected = ResourceTools.getResourceAsString("Hibernate6ToolsTest-testGenerateSqlSchema_WithoutUnderscore-expected.sql", this.getClass());
        String actual = FileTools.getFileAsString(outputSqlFile);
        AssertTools.assertIgnoreLineFeed(expected, actual);
    }

    @Test
    public void testGenerateSqlSchema_WithUnderscore() throws Exception {
        System.setProperty("hibernate.dialect.storage_engine", "innodb");
        String outputSqlFile = File.createTempFile("output", ".sql").getAbsolutePath();
        Hibernate63Tools.generateSqlSchema(MySQLDialect.class, outputSqlFile, true, "com.foilen.smalltools.tools.test.entities");

        String expected = ResourceTools.getResourceAsString("Hibernate6ToolsTest-testGenerateSqlSchema_WithUnderscore-expected.sql", this.getClass());
        String actual = FileTools.getFileAsString(outputSqlFile);
        AssertTools.assertIgnoreLineFeed(expected, actual);
    }

}
