package com.foilen.smalltools.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Tests for {@link ResourceTools}.
 */
public class ResourceToolsTest {

    @Test
    public void testGetResourceAsString() {
        String expected = "This is a test";
        String actual = ResourceTools.getResourceAsString("/com/foilen/smalltools/tools/ResourceToolsTest-getResourceString.txt");

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGetResourceAsStringNotExists() {
        assertThrows(SmallToolsException.class, () -> {
            ResourceTools.getResourceAsString("does_not_exists.txt");
        });
    }

    @Test
    public void testGetResourceAsStringWithContext() {
        String expected = "This is a test";
        String actual = ResourceTools.getResourceAsString("ResourceToolsTest-getResourceString.txt", this.getClass());

        Assertions.assertEquals(expected, actual);
    }

}
