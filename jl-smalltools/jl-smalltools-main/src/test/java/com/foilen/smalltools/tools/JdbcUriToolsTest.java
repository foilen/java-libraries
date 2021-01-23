/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class JdbcUriToolsTest {

    @Test
    public void test_mariadb() {
        JdbcUriTools jdbcUri = new JdbcUriTools("jdbc:mariadb://localhost:3306/DB?user=root&password=myPassword");
        AssertTools.assertJsonComparison("JdbcUriTest-test_mariadb.json", getClass(), jdbcUri);
        Assert.assertEquals("jdbc:mariadb://localhost:3306/DB?password=myPassword&user=root", jdbcUri.toUri());
    }

    @Test
    public void test_mongo_full() {
        JdbcUriTools jdbcUri = new JdbcUriTools("jdbc:mongodb://root:ABC@localhost:27017,localhost:27018,localhost:27019/myDb?replicaSet=rs&connectTimeoutMS=300000");
        AssertTools.assertJsonComparison("JdbcUriTest-test_mongo_full.json", getClass(), jdbcUri);
        Assert.assertEquals("jdbc:mongodb://root:ABC@localhost:27017,localhost:27018,localhost:27019/myDb?connectTimeoutMS=300000&replicaSet=rs", jdbcUri.toUri());
    }

    @Test
    public void test_mongo_kerberos() {
        JdbcUriTools jdbcUri = new JdbcUriTools(
                "jdbc:mongodb://username%40REALM.com@localhost:27017/?authMechanism=GSSAPI&authSource=$external&authMechanismProperties=SERVICE_NAME:mongo,CANONICALIZE_HOST_NAME:true&gssapiServiceName=mongo");
        AssertTools.assertJsonComparison("JdbcUriTest-test_mongo_kerberos.json", getClass(), jdbcUri);
        Assert.assertEquals(
                "jdbc:mongodb://username%40REALM.com@localhost:27017/?authMechanism=GSSAPI&authMechanismProperties=SERVICE_NAME:mongo,CANONICALIZE_HOST_NAME:true&authSource=$external&gssapiServiceName=mongo",
                jdbcUri.toUri());
    }

    @Test
    public void test_mongo_local() {
        JdbcUriTools jdbcUri = new JdbcUriTools("jdbc:mongodb://localhost:27017");
        AssertTools.assertJsonComparison("JdbcUriTest-test_mongo_local.json", getClass(), jdbcUri);
        Assert.assertEquals("jdbc:mongodb://localhost:27017", jdbcUri.toUri());
    }

}
