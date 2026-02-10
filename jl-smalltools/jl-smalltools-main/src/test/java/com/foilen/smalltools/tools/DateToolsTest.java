package com.foilen.smalltools.tools;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class DateToolsTest {

    @Test
    public void testAddDate() throws Exception {
        Assertions.assertEquals(DateTools.parseFull("2000-01-10 01:00:00"), DateTools.addDate(DateTools.parseFull("2000-01-01 01:00:00"), Calendar.DAY_OF_MONTH, 9));
        Assertions.assertEquals(DateTools.parseFull("2000-02-10 01:00:00"), DateTools.addDate(DateTools.parseFull("2000-01-10 01:00:00"), Calendar.MONTH, 1));
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
        Assertions.assertTrue(DateTools.isAfter(DateTools.parseFull("2000-01-31 01:00:00"), DateTools.parseFull("2000-01-01 01:00:00")));
        Assertions.assertFalse(DateTools.isAfter(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-31 01:00:00")));
    }

    @Test
    public void testIsBefore() throws Exception {
        Assertions.assertTrue(DateTools.isBefore(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-31 01:00:00")));
        Assertions.assertFalse(DateTools.isBefore(DateTools.parseFull("2000-01-31 01:00:00"), DateTools.parseFull("2000-01-01 01:00:00")));
    }

    @Test
    public void testIsExpired() throws Exception {

        // One month later
        Assertions.assertFalse(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-31 01:00:00"), Calendar.MONTH, 1));
        Assertions.assertFalse(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-02-01 00:00:00"), Calendar.MONTH, 1));
        Assertions.assertTrue(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-02-01 01:00:00"), Calendar.MONTH, 1));
        Assertions.assertTrue(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-02-02 00:00:00"), Calendar.MONTH, 1));

        // 2 days later
        Assertions.assertFalse(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-02 01:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assertions.assertFalse(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-03 00:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assertions.assertTrue(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-03 01:00:00"), Calendar.DAY_OF_MONTH, 2));
        Assertions.assertTrue(DateTools.isExpired(DateTools.parseFull("2000-01-01 01:00:00"), DateTools.parseFull("2000-01-04 00:00:00"), Calendar.DAY_OF_MONTH, 2));

    }

    @Test
    public void testParseAndFormat() {
        String text = "2015-05-04 04:55:23";
        Date date = DateTools.parseFull(text);
        Assertions.assertNotNull(date);

        Assertions.assertEquals("2015-05-04 04:55:23", DateTools.formatFull(date));
        Assertions.assertEquals("2015-05-04", DateTools.formatDateOnly(date));
        Assertions.assertEquals("04:55:23", DateTools.formatTimeOnly(date));

        Assertions.assertEquals(DateTools.parseFull("2015-05-04 00:00:00"), DateTools.parseDateOnly("2015-05-04"));
        Assertions.assertEquals(DateTools.parseFull("1970-01-01 04:55:23"), DateTools.parseTimeOnly("04:55:23"));

        Assertions.assertNull(DateTools.parseDateOnly(null));
        Assertions.assertNull(DateTools.parseFull(null));
        Assertions.assertNull(DateTools.parseTimeOnly(null));
        Assertions.assertNull(DateTools.formatDateOnly(null));
        Assertions.assertNull(DateTools.formatFull(null));
        Assertions.assertNull(DateTools.formatTimeOnly(null));

    }

}
