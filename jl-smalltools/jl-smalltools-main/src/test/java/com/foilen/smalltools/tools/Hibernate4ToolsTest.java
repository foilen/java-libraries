/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;

import org.hibernate.dialect.MySQLDialect;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class Hibernate4ToolsTest {

    @Test
    public void testGenerateSqlSchema_WithoutUnderscore() throws Exception {
        String outputSqlFile = File.createTempFile("output", ".sql").getAbsolutePath();
        Hibernate4Tools.generateSqlSchema(MySQLDialect.class, outputSqlFile, false, "com.foilen.smalltools.tools.test.entities");

        String expected = ResourceTools.getResourceAsString("Hibernate4ToolsTest-testGenerateSqlSchema_WithoutUnderscore-expected.sql", this.getClass());
        String actual = FileTools.getFileAsString(outputSqlFile);
        AssertTools.assertIgnoreLineFeed(expected, actual);
    }

    @Test
    public void testGenerateSqlSchema_WithUnderscore() throws Exception {
        String outputSqlFile = File.createTempFile("output", ".sql").getAbsolutePath();
        Hibernate4Tools.generateSqlSchema(MySQLDialect.class, outputSqlFile, true, "com.foilen.smalltools.tools.test.entities");

        String expected = ResourceTools.getResourceAsString("Hibernate4ToolsTest-testGenerateSqlSchema_WithUnderscore-expected.sql", this.getClass());
        String actual = FileTools.getFileAsString(outputSqlFile);
        AssertTools.assertIgnoreLineFeed(expected, actual);
    }

}
