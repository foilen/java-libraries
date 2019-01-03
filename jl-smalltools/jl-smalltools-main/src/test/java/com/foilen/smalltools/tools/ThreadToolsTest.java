/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tuple.Tuple2;

public class ThreadToolsTest {

    private void assertThreadName(String expected) {
        Assert.assertEquals(expected, Thread.currentThread().getName());
    }

    @Test
    public void testNameThread() {

        // No change
        String initialName = Thread.currentThread().getName();
        ThreadTools.nameThread().change();
        assertThreadName(initialName);

        // Change name and revert
        ThreadNameStateTool state = ThreadTools.nameThread();
        state.clear().appendText("SomeText").change();
        assertThreadName("SomeText");
        state.clear().appendText("SomeText 2").change();
        assertThreadName("SomeText 2");
        state.revert();
        assertThreadName("SomeText");
        state.revert();
        assertThreadName("SomeText 2");

        // Numbers
        state.clear().appendText("Text and ").appendObjectText(10).change();
        assertThreadName("Text and 10");

        // JSON
        state.clear().appendText("Text and ").appendJson(new Tuple2<>("text", 66)).change();
        assertThreadName("Text and {\"a\":\"text\",\"b\":66}");

        // Class name
        state.clear().appendObjectClass(this).change();
        assertThreadName("com.foilen.smalltools.tools.ThreadToolsTest");

        // Now time
        state.clear().appendDate(new Date(0)).change();
        assertThreadName("1969-12-31 19:00:00");
    }

    @Test
    public void testNameThread_pop() {

        ThreadNameStateTool state = ThreadTools.nameThread().setSeparator("-");
        state.clear().appendText("SomeText").appendText("First Time").change();
        assertThreadName("SomeText-First Time");

        state.appendText("Yay").change();
        assertThreadName("SomeText-First Time-Yay");

        state.pop().appendText("Yo").change();
        assertThreadName("SomeText-First Time-Yo");

        state.revert();
        assertThreadName("SomeText-First Time-Yay");
        state.pop().pop().appendText("Cool").change();
        assertThreadName("SomeText-Cool");

        state.pop().pop().pop().pop().appendText("Cool").change();
        assertThreadName("Cool");
    }

}
