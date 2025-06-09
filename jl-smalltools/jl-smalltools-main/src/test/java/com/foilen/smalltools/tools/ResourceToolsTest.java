package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Tests for {@link ResourceTools}.
 */
public class ResourceToolsTest {

    @Test
    public void testGetResourceAsString() {
        String expected = "This is a test";
        String actual = ResourceTools.getResourceAsString("/com/foilen/smalltools/tools/ResourceToolsTest-getResourceString.txt");

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = SmallToolsException.class)
    public void testGetResourceAsStringNotExists() {
        ResourceTools.getResourceAsString("does_not_exists.txt");
    }

    @Test
    public void testGetResourceAsStringWithContext() {
        String expected = "This is a test";
        String actual = ResourceTools.getResourceAsString("ResourceToolsTest-getResourceString.txt", this.getClass());

        Assert.assertEquals(expected, actual);
    }

}
