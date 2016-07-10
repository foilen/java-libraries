/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.util.Date;

public class ThreadNameStateTool {

    private String previousName;
    private StringBuilder nextName;

    public ThreadNameStateTool() {
        previousName = Thread.currentThread().getName();
        nextName = new StringBuilder(previousName);
    }

    /**
     * Append the current full date and time.
     * 
     * @return this
     */
    public ThreadNameStateTool appendDate() {
        appendDate(new Date());
        return this;
    }

    /**
     * Append the full date and time.
     * 
     * @param date
     *            the date
     * @return this
     */
    public ThreadNameStateTool appendDate(Date date) {
        if (date == null) {
            nextName.append("null");
        } else {
            nextName.append(DateTools.formatFull(date));
        }
        return this;
    }

    /**
     * Append an object as a compact JSON.
     * 
     * @param object
     *            the object
     * @return this
     */
    public ThreadNameStateTool appendJson(Object object) {
        if (object == null) {
            nextName.append("null");
        } else {
            nextName.append(JsonTools.compactPrint(object));
        }
        return this;
    }

    /**
     * Append the class name.
     * 
     * @param object
     *            the object
     * @return this
     */
    public ThreadNameStateTool appendObjectClass(Object object) {
        if (object == null) {
            nextName.append("null");
        } else {
            nextName.append(object.getClass().getName());
        }
        return this;
    }

    /**
     * Append an object {@link #toString()}.
     * 
     * @param object
     *            the object
     * @return this
     */
    public ThreadNameStateTool appendObjectText(Object object) {
        if (object == null) {
            nextName.append("null");
        } else {
            nextName.append(object.toString());
        }
        return this;
    }

    /**
     * Append some text.
     * 
     * @param text
     *            the text
     * @return this
     */
    public ThreadNameStateTool appendText(String text) {
        nextName.append(text);
        return this;
    }

    /**
     * Change the current thread name. Can be reverted with {@link #revert()}.
     * 
     * @return this
     */
    public ThreadNameStateTool change() {
        previousName = Thread.currentThread().getName();
        Thread.currentThread().setName(nextName.toString());
        return this;
    }

    /**
     * Remove all the text for the next {@link #change()}.
     * 
     * @return this
     */
    public ThreadNameStateTool clear() {
        nextName = new StringBuilder();
        return this;
    }

    /**
     * Change the current thread name back to what it was before the last {@link #change()}. Calling a second time switch back to the name before the last {@link #revert()}.
     * 
     * @return this
     */
    public ThreadNameStateTool revert() {
        String tmp = previousName;
        previousName = Thread.currentThread().getName();
        Thread.currentThread().setName(tmp);
        return this;
    }

}