/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class DateToolsTest {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testAddDate() throws Exception {
        Assert.assertEquals(SDF.parse("2000-01-10 01:00:00"), DateTools.addDate(SDF.parse("2000-01-01 01:00:00"), Calendar.DAY_OF_MONTH, 9));
        Assert.assertEquals(SDF.parse("2000-02-10 01:00:00"), DateTools.addDate(SDF.parse("2000-01-10 01:00:00"), Calendar.MONTH, 1));
    }

    @Test
    public void testFullParseAndFormat() {
        String text = "2015-05-04 04:55:23";
        Date date = DateTools.parseFull(text);
        Assert.assertNotNull(date);

        String actual = DateTools.formatFull(date);
        Assert.assertEquals(text, actual);
    }

    @Test
    public void testIsAfter() throws Exception {
        Assert.assertTrue(DateTools.isAfter(SDF.parse("2000-01-31 01:00:00"), SDF.parse("2000-01-01 01:00:00")));
        Assert.assertFalse(DateTools.isAfter(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-01-31 01:00:00")));
    }

    @Test
    public void testIsBefore() throws Exception {
        Assert.assertTrue(DateTools.isBefore(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-01-31 01:00:00")));
        Assert.assertFalse(DateTools.isBefore(SDF.parse("2000-01-31 01:00:00"), SDF.parse("2000-01-01 01:00:00")));
    }

    @Test
    public void testIsExpired() throws Exception {

        // One month later
        Assert.assertFalse(DateTools.isExpired(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-01-31 01:00:00"), Calendar.MONTH, 1));
        Assert.assertFalse(DateTools.isExpired(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-02-01 00:00:00"), Calendar.MONTH, 1));
        Assert.assertTrue(DateTools.isExpired(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-02-01 01:00:00"), Calendar.MONTH, 1));
        Assert.assertTrue(DateTools.isExpired(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-02-02 00:00:00"), Calendar.MONTH, 1));

        // 2 days later
        Assert.assertFalse(DateTools.isExpired(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-01-02 01:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assert.assertFalse(DateTools.isExpired(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-01-03 00:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assert.assertTrue(DateTools.isExpired(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-01-03 01:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assert.assertTrue(DateTools.isExpired(SDF.parse("2000-01-01 01:00:00"), SDF.parse("2000-01-04 00:00:00"), Calendar.DAY_OF_MONTH, 2));

    }

}
