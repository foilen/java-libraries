/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.util.Calendar;
import java.util.Date;

/**
 * Some common methods to manage dates.
 */
public final class DateTools {

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
     * @return true if is expired ( dateToCheck + delta < currentTime)
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
     * @return true if is expired ( dateToCheck + delta < now)
     */
    public static boolean isExpired(Date dateToCheck, int calendarUnit, int delta) {
        return isExpired(dateToCheck, new Date(), calendarUnit, delta);
    }

    private DateTools() {
    }
}
