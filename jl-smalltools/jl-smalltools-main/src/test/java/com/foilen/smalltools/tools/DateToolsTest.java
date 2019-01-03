/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class DateToolsTest {

    @Test
    public void testAddDate() throws Exception {
        Assert.assertEquals(DateTools.parseFull("2000-01-10 01:00:00"), DateTools.addDate(DateTools.parseFull("2000-01-01 01:00:00"), Calendar.DAY_OF_MONTH, 9));
        Assert.assertEquals(DateTools.parseFull("2000-02-10 01:00:00"), DateTools.addDate(DateTools.parseFull("2000-01-10 01:00:00"), Calendar.MONTH, 1));
    }

    @Test
    public void testAddDate_Now() throws Exception {
        AssertTools.assertEqualsDelta( //
                DateTools.addDate(new Date(), Calendar.DAY_OF_MONTH, 9).getTime(), //
                DateTools.addDate(Calendar.DAY_OF_MONTH, 9).getTime(), //
                1000L);
    }

    @Test
    public void testIsAfter() throws Exception {
        Assert.assertTrue(DateTools.isAfter(DateTools.parseFull("2000-01-31 01:00:00"), DateTools.parseFull("2000-01-01 01:00:00")));
        Assert.assertFalse(DateTools.isAfter(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-31 01:00:00")));
    }

    @Test
    public void testIsBefore() throws Exception {
        Assert.assertTrue(DateTools.isBefore(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-31 01:00:00")));
        Assert.assertFalse(DateTools.isBefore(DateTools.parseFull("2000-01-31 01:00:00"), DateTools.parseFull("2000-01-01 01:00:00")));
    }

    @Test
    public void testIsExpired() throws Exception {

        // One month later
        Assert.assertFalse(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-31 01:00:00"), Calendar.MONTH, 1));
        Assert.assertFalse(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-02-01 00:00:00"), Calendar.MONTH, 1));
        Assert.assertTrue(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-02-01 01:00:00"), Calendar.MONTH, 1));
        Assert.assertTrue(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-02-02 00:00:00"), Calendar.MONTH, 1));

        // 2 days later
        Assert.assertFalse(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-02 01:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assert.assertFalse(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-03 00:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assert.assertTrue(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-03 01:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assert.assertTrue(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-04 00:00:00"), Calendar.DAY_OF_MONTH, 2));

    }

    @Test
    public void testParseAndFormat() {
        String text = "2015-05-04 04:55:23";
        Date date = DateTools.parseFull(text);
        Assert.assertNotNull(date);

        Assert.assertEquals("2015-05-04 04:55:23", DateTools.formatFull(date));
        Assert.assertEquals("2015-05-04", DateTools.formatDateOnly(date));
        Assert.assertEquals("04:55:23", DateTools.formatTimeOnly(date));

        Assert.assertEquals(DateTools.parseFull("2015-05-04 00:00:00"), DateTools.parseDateOnly("2015-05-04"));
        Assert.assertEquals(DateTools.parseFull("1970-01-01 04:55:23"), DateTools.parseTimeOnly("04:55:23"));

        Assert.assertNull(DateTools.parseDateOnly(null));
        Assert.assertNull(DateTools.parseFull(null));
        Assert.assertNull(DateTools.parseTimeOnly(null));
        Assert.assertNull(DateTools.formatDateOnly(null));
        Assert.assertNull(DateTools.formatFull(null));
        Assert.assertNull(DateTools.formatTimeOnly(null));

    }

}
