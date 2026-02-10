package com.foilen.smalltools.tools;

import java.util.Date;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Tests for {@link AssertTools}.
 */
public class AssertToolsTest {

    @Test
    public void testAssertFalseBooleanFail() {
        SmallToolsException exception = assertThrows(SmallToolsException.class, () -> {
            AssertTools.assertFalse(true);
        });
        assertTrue(exception.getMessage().contains("Value must be false"));
    }

    @Test
    public void testAssertFalseBooleanStringFail() {
        SmallToolsException exception = assertThrows(SmallToolsException.class, () -> {
            AssertTools.assertFalse(true, "Custom");
        });
        assertTrue(exception.getMessage().contains("Custom"));
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
        SmallToolsException exception = assertThrows(SmallToolsException.class, () -> {
            AssertTools.assertNotNull(null);
        });
        assertTrue(exception.getMessage().contains("Value must not be null"));
    }

    @Test
    public void testAssertNotNullBooleanStringFail() {
        SmallToolsException exception = assertThrows(SmallToolsException.class, () -> {
            AssertTools.assertNotNull(null, "Custom");
        });
        assertTrue(exception.getMessage().contains("Custom"));
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
        SmallToolsException exception = assertThrows(SmallToolsException.class, () -> {
            AssertTools.assertNull(new Date());
        });
        assertTrue(exception.getMessage().contains("Value must be null"));
    }

    @Test
    public void testAssertNullBooleanStringFail() {
        SmallToolsException exception = assertThrows(SmallToolsException.class, () -> {
            AssertTools.assertNull(new Date(), "Custom");
        });
        assertTrue(exception.getMessage().contains("Custom"));
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
        SmallToolsException exception = assertThrows(SmallToolsException.class, () -> {
            AssertTools.assertTrue(false);
        });
        assertTrue(exception.getMessage().contains("Value must be true"));
    }

    @Test
    public void testAssertTrueBooleanStringFail() {
        SmallToolsException exception = assertThrows(SmallToolsException.class, () -> {
            AssertTools.assertTrue(false, "Custom");
        });
        assertTrue(exception.getMessage().contains("Custom"));
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
