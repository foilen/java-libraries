/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.db;

public class TestInt implements TestParent {

    private int theInt;

    public TestInt() {
    }

    public TestInt(int theInt) {
        this.theInt = theInt;
    }

    public int getTheInt() {
        return theInt;
    }

    public void setTheInt(int theInt) {
        this.theInt = theInt;
    }

}
