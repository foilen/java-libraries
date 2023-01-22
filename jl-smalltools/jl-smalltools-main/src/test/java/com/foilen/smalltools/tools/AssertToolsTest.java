/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Tests for {@link AssertTools}.
 */
public class AssertToolsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAssertFalseBooleanFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Value must be false");
        AssertTools.assertFalse(true);
    }

    @Test
    public void testAssertFalseBooleanStringFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Custom");
        AssertTools.assertFalse(true, "Custom");
    }

    @Test
    public void testAssertFalseBooleanStringSuccess() {
        AssertTools.assertFalse(false, "Custom");
    }

    @Test
    public void testAssertFalseBooleanSuccess() {
        AssertTools.assertFalse(false);
    }

    @Test
    public void testAssertNotNullBooleanFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Value must not be null");
        AssertTools.assertNotNull(null);
    }

    @Test
    public void testAssertNotNullBooleanStringFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Custom");
        AssertTools.assertNotNull(null, "Custom");
    }

    @Test
    public void testAssertNotNullBooleanStringSuccess() {
        AssertTools.assertNotNull(new Date(), "Custom");
    }

    @Test
    public void testAssertNotNullBooleanSuccess() {
        AssertTools.assertNotNull(new Date());
    }

    @Test
    public void testAssertNullBooleanFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Value must be null");
        AssertTools.assertNull(new Date());
    }

    @Test
    public void testAssertNullBooleanStringFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Custom");
        AssertTools.assertNull(new Date(), "Custom");
    }

    @Test
    public void testAssertNullBooleanStringSuccess() {
        AssertTools.assertNull(null, "Custom");
    }

    @Test
    public void testAssertNullBooleanSuccess() {
        AssertTools.assertNull(null);
    }

    @Test
    public void testAssertTrueBooleanFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Value must be true");
        AssertTools.assertTrue(false);
    }

    @Test
    public void testAssertTrueBooleanStringFail() {
        thrown.expect(SmallToolsException.class);
        thrown.expectMessage("Custom");
        AssertTools.assertTrue(false, "Custom");
    }

    @Test
    public void testAssertTrueBooleanStringSuccess() {
        AssertTools.assertTrue(true, "Custom");
    }

    @Test
    public void testAssertTrueBooleanSuccess() {
        AssertTools.assertTrue(true);
    }

}
