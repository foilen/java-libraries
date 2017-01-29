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

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some common methods to manage dates.
 */
public final class DateTools {

    private static final ThreadLocal<SimpleDateFormat> sdfFull = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    /**
     * Add some delta to the date.
     * 
     * @param date
     *            the date
     * @param calendarUnit
     *            the unit of the delta that is a constant on {@link Calendar}
     * @param delta
     *            the delta
     * @return the new date
     */
    public static Date addDate(Date date, int calendarUnit, int delta) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendarUnit, delta);
        return calendar.getTime();
    }

    /**
     * Format a date to "yyyy-MM-dd HH:mm:ss" format.
     * 
     * @param date
     *            the date
     * @return the text date
     */
    public static String formatFull(Date date) {
        try {
            return sdfFull.get().format(date);
        } catch (Exception e) {
            throw new SmallToolsException("Could not format [" + date + "] with full format", e);
        }
    }

    /**
     * Check that the date after is really after.
     * 
     * @param dateAfter
     *            the date that should be after
     * @param dateBefore
     *            the date that should be before
     * @return true if really after
     */
    public static boolean isAfter(Date dateAfter, Date dateBefore) {
        return dateBefore.getTime() <= dateAfter.getTime();
    }

    /**
     * Check that the date before is really before.
     * 
     * @param dateBefore
     *            the date that should be before
     * @param dateAfter
     *            the date that should be after
     * @return true if really before
     */
    public static boolean isBefore(Date dateBefore, Date dateAfter) {
        return dateBefore.getTime() <= dateAfter.getTime();
    }

    /**
     * Tell if the date to check is now expired if we add the delta.
     * 
     * @param dateToCheck
     *            the date to check the expiration
     * @param currentTime
     *            the date considered as now
     * @param calendarUnit
     *            the unit of the delta that is a constant on {@link Calendar}
     * @param delta
     *            the delta
     * @return true if is expired ( dateToCheck + delta &lt; currentTime)
     */
    public static boolean isExpired(Date dateToCheck, Date currentTime, int calendarUnit, int delta) {
        Date expiresOn = addDate(dateToCheck, calendarUnit, delta);
        return expiresOn.getTime() <= currentTime.getTime();
    }

    /**
     * Tell if the date to check is now expired if we add the delta.
     * 
     * @param dateToCheck
     *            the date to check the expiration
     * @param calendarUnit
     *            the unit of the delta that is a constant on {@link Calendar}
     * @param delta
     *            the delta
     * @return true if is expired ( dateToCheck + delta &lt; now)
     */
    public static boolean isExpired(Date dateToCheck, int calendarUnit, int delta) {
        return isExpired(dateToCheck, new Date(), calendarUnit, delta);
    }

    /**
     * Parse a date with "yyyy-MM-dd HH:mm:ss" format.
     * 
     * @param date
     *            the date in text
     * @return the date
     */
    public static Date parseFull(String date) {
        try {
            return sdfFull.get().parse(date);
        } catch (Exception e) {
            throw new SmallToolsException("Could not parse [" + date + "] with full format", e);
        }
    }

    private DateTools() {
    }
}