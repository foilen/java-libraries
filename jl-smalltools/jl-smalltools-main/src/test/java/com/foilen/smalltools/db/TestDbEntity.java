/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.db;

import org.junit.Assert;

import com.foilen.smalltools.tools.AbstractBasics;

public class TestDbEntity extends AbstractBasics {

    private String id;
    private int number;

    public TestDbEntity() {
    }

    public TestDbEntity(String id, int number) {
        this.id = id;
        this.number = number;
    }

    public void assertValue(String expectedId, int expectedNumber) {
        Assert.assertEquals(expectedId, id);
        Assert.assertEquals(expectedNumber, number);
    }

    public String getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}
