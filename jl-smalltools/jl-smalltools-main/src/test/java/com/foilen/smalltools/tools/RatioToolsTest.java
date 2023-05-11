/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.junit.Assert;
import org.junit.Test;

public class RatioToolsTest {

    @Test
    public void testGetDownScaleByMaxSide_byLongerSide_widthWise() {
        // Initially 400x200
        // Fit in max 300x200 (reduce the 400 to 300 -> 300x150)
        Assert.assertEquals(0.75f, RatioTools.getDownScaleByMaxSide(400, 200, 300, 200), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxSide_byLongerSide_heightWise() {
        // Initially 200x400
        // Fit in max 300x200 (reduce the 400 to 300 -> 150x300)
        Assert.assertEquals(0.75f, RatioTools.getDownScaleByMaxSide(200, 400, 300, 200), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxSide_byShorterSide_widthWise() {
        // Initially 200x400
        // Fit in max 300x120 (reduce the 200 to 120 -> 120x240)
        Assert.assertEquals(0.60f, RatioTools.getDownScaleByMaxSide(200, 400, 300, 120), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxSide_byShorterSide_heightWise() {
        // Initially 400x200
        // Fit in max 300x120 (reduce the 200 to 120 -> 240x120)
        Assert.assertEquals(0.60f, RatioTools.getDownScaleByMaxSide(400, 200, 300, 120), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxSide_alreadyFit_1() {
        // Initially 290x100
        // Fit in max 300x120
        Assert.assertEquals(1f, RatioTools.getDownScaleByMaxSide(290, 100, 300, 120), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxSide_alreadyFit_2() {
        // Initially 100x290
        // Fit in max 300x120
        Assert.assertEquals(1f, RatioTools.getDownScaleByMaxSide(100, 290, 300, 120), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxHeight_smaller() {
        // Initially 400x200
        // Max height 300 stay the same)
        Assert.assertEquals(1f, RatioTools.getDownScaleByMaxHeight(400, 200, 300), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxHeight_reduce() {
        // Initially 200x400
        // Max height 300 (reduce the 400 -> 150x300)
        Assert.assertEquals( 0.75f, RatioTools.getDownScaleByMaxHeight(200, 400, 300), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxWidth_reduce() {
        // Initially 400x200
        // Max width 300 (reduce the 400 to 300 -> 300x150)
        Assert.assertEquals(0.75f, RatioTools.getDownScaleByMaxWidth(400, 200, 300), 0.0001f);
    }

    @Test
    public void testGetDownScaleByMaxWidth_smaller() {
        // Initially 200x400
        // Max width 300 (stay the same)
        Assert.assertEquals(1f, RatioTools.getDownScaleByMaxWidth(200, 400, 300), 0.0001f);
    }


}