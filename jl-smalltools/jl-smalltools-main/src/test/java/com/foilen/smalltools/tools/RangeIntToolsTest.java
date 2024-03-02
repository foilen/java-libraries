/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.test.asserts.AssertTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class RangeIntToolsTest {

    @Test
    public void test() {
        // Empty
        var rangeTool = new RangeIntTools();
        AssertTools.assertJsonComparison(List.of(), rangeTool.getRanges());
        Assert.assertFalse(rangeTool.isInRange(0));
        Assert.assertFalse(rangeTool.isInRange(1));

        // 3 ranges (1-3, 5-7 , 9-11)
        rangeTool.addValue(11);
        rangeTool.addValue(10);
        rangeTool.addValue(9);
        rangeTool.addValue(5);
        rangeTool.addValue(2);
        rangeTool.addValue(1);
        rangeTool.addValue(5);
        rangeTool.addValue(7);
        rangeTool.addValue(3);
        rangeTool.addValue(6);
        AssertTools.assertJsonComparison(List.of(
                new RangeInt(1, 3),
                new RangeInt(5, 7),
                new RangeInt(9, 11)
        ), rangeTool.getRanges());
        Assert.assertFalse(rangeTool.isInRange(0));
        for (int i = 1; i <= 3; ++i)
            Assert.assertTrue(rangeTool.isInRange(i));
        Assert.assertFalse(rangeTool.isInRange(4));
        for (int i = 5; i <= 7; ++i)
            Assert.assertTrue(rangeTool.isInRange(i));
        Assert.assertFalse(rangeTool.isInRange(8));
        for (int i = 9; i <= 11; ++i)
            Assert.assertTrue(rangeTool.isInRange(i));
        Assert.assertFalse(rangeTool.isInRange(12));

    }

    @Test
    public void testCreate1BigRangeFromALotAscending() {
        var rangeTool = new RangeIntTools();
        for (int i = 1; i <= 10000; i += 3) {
            rangeTool.addValue(i);
        }
        for (int i = 2; i <= 10000; i += 3) {
            rangeTool.addValue(i);
        }
        for (int i = 3; i <= 10000; i += 3) {
            rangeTool.addValue(i);
        }
        AssertTools.assertJsonComparison(List.of(
                new RangeInt(1, 10000)
        ), rangeTool.getRanges());
    }

    @Test
    public void testCreate1BigRangeFromALotDescending() {
        var rangeTool = new RangeIntTools();
        for (int i = 10000; i >= 1; i -= 3) {
            rangeTool.addValue(i);
        }
        for (int i = 10000 - 1; i >= 1; i -= 3) {
            rangeTool.addValue(i);
        }
        for (int i = 10000 - 2; i >= 1; i -= 3) {
            rangeTool.addValue(i);
        }
        AssertTools.assertJsonComparison(List.of(
                new RangeInt(1, 10000)
        ), rangeTool.getRanges());
    }

}