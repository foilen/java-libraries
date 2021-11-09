/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.db;

import org.junit.Assert;

import com.foilen.smalltools.tools.AbstractBasics;

public class TestDbEntityWithPolymorphism extends AbstractBasics {

    private String id;
    private int number;
    private TestParent poly;

    public TestDbEntityWithPolymorphism() {
    }

    public TestDbEntityWithPolymorphism(String id, int number) {
        this.id = id;
        this.number = number;
    }

    public TestDbEntityWithPolymorphism(String id, int number, TestParent poly) {
        this.id = id;
        this.number = number;
        this.poly = poly;
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

    public TestParent getPoly() {
        return poly;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setPoly(TestParent poly) {
        this.poly = poly;
    }

}
