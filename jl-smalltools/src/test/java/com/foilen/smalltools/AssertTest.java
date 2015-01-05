/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.foilen.smalltools.Assert;
import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Tests for {@link Assert}.
 */
public class AssertTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAssertFalseBooleanFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Value must be false");
        Assert.assertFalse(true);
    }

    @Test
    public void testAssertFalseBooleanStringFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Custom");
        Assert.assertFalse(true, "Custom");
    }

    @Test
    public void testAssertFalseBooleanStringSuccess() {
        Assert.assertFalse(false, "Custom");
    }

    @Test
    public void testAssertFalseBooleanSuccess() {
        Assert.assertFalse(false);
    }

    @Test
    public void testAssertTrueBooleanFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Value must be true");
        Assert.assertTrue(false);
    }

    @Test
    public void testAssertTrueBooleanStringFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Custom");
        Assert.assertTrue(false, "Custom");
    }

    @Test
    public void testAssertTrueBooleanStringSuccess() {
        Assert.assertTrue(true, "Custom");
    }

    @Test
    public void testAssertTrueBooleanSuccess() {
        Assert.assertTrue(true);
    }

    @Test
    public void testAssertNullBooleanFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Value must be null");
        Assert.assertNull(new Date());
    }

    @Test
    public void testAssertNullBooleanStringFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Custom");
        Assert.assertNull(new Date(), "Custom");
    }

    @Test
    public void testAssertNullBooleanStringSuccess() {
        Assert.assertNull(null, "Custom");
    }

    @Test
    public void testAssertNullBooleanSuccess() {
        Assert.assertNull(null);
    }

    @Test
    public void testAssertNotNullBooleanFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Value must not be null");
        Assert.assertNotNull(null);
    }

    @Test
    public void testAssertNotNullBooleanStringFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Custom");
        Assert.assertNotNull(null, "Custom");
    }

    @Test
    public void testAssertNotNullBooleanStringSuccess() {
        Assert.assertNotNull(new Date(), "Custom");
    }

    @Test
    public void testAssertNotNullBooleanSuccess() {
        Assert.assertNotNull(new Date());
    }

}
