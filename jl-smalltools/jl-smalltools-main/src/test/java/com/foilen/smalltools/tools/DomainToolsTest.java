/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class DomainToolsTest {

    @Test
    public void testGetParts() {
        AssertTools.assertJsonComparison(null, DomainTools.getParts(null));
        AssertTools.assertJsonComparison(Arrays.asList("test", "foilen", "com"), DomainTools.getParts("test.foilen.com"));
    }

    @Test
    public void testGetPartsAsList() {
        AssertTools.assertJsonComparison(null, DomainTools.getPartsAsList(null));
        AssertTools.assertJsonComparison(Arrays.asList("test", "foilen", "com"), DomainTools.getPartsAsList("test.foilen.com"));
    }

    @Test
    public void testGetReverseParts() {
        AssertTools.assertJsonComparison(null, DomainTools.getReverseParts(null));
        AssertTools.assertJsonComparison(Arrays.asList("com", "foilen", "test"), DomainTools.getReverseParts("test.foilen.com"));
    }

    @Test
    public void testGetReversePartsAsList() {
        AssertTools.assertJsonComparison(null, DomainTools.getReversePartsAsList(null));
        AssertTools.assertJsonComparison(Arrays.asList("com", "foilen", "test"), DomainTools.getReversePartsAsList("test.foilen.com"));
    }

    @Test
    public void testReverse() {
        Assert.assertEquals(null, DomainTools.reverse(null));
        Assert.assertEquals("", DomainTools.reverse(""));
        Assert.assertEquals("com", DomainTools.reverse("com"));
        Assert.assertEquals("com.foilen", DomainTools.reverse("foilen.com"));
        Assert.assertEquals("com.foilen.test", DomainTools.reverse("test.foilen.com"));
        Assert.assertEquals("com.foilen.test.sub", DomainTools.reverse("sub.test.foilen.com"));
    }

}
