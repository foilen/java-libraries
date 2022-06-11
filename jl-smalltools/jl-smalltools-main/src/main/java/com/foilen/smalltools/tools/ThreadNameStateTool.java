/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;

public class ThreadNameStateTool {

    private List<String> previousName;
    private List<String> currentName;
    private List<String> nextName;
    private String separator = "";

    public ThreadNameStateTool() {
        previousName = new ArrayList<>();
        previousName.add(Thread.currentThread().getName());
        currentName = new ArrayList<>();
        currentName.add(Thread.currentThread().getName());
        nextName = new ArrayList<>();
        nextName.add(Thread.currentThread().getName());
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
            nextName.add("null");
        } else {
            nextName.add(DateTools.formatFull(date));
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
            nextName.add("null");
        } else {
            nextName.add(JsonTools.compactPrint(object));
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
            nextName.add("null");
        } else {
            nextName.add(object.getClass().getName());
        }
        return this;
    }

    /**
     * Append the simple class name.
     *
     * @param object
     *            the object
     * @return this
     */
    public ThreadNameStateTool appendObjectClassSimple(Object object) {
        if (object == null) {
            nextName.add("null");
        } else {
            nextName.add(object.getClass().getSimpleName());
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
            nextName.add("null");
        } else {
            nextName.add(object.toString());
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
        nextName.add(text);
        return this;
    }

    /**
     * Change the current thread name. Can be reverted with {@link #revert()}.
     *
     * @return this
     */
    public ThreadNameStateTool change() {
        previousName.clear();
        previousName.addAll(currentName);
        currentName.clear();
        currentName.addAll(nextName);
        Thread.currentThread().setName(Joiner.on(separator).join(nextName));
        return this;
    }

    /**
     * Remove all the text for the next {@link #change()}.
     *
     * @return this
     */
    public ThreadNameStateTool clear() {
        nextName = new ArrayList<>();
        return this;
    }

    /**
     * Remove the last part.
     *
     * @return this
     */
    public ThreadNameStateTool pop() {
        if (!nextName.isEmpty()) {
            nextName.remove(nextName.size() - 1);
        }
        return this;
    }

    /**
     * Change the current thread name back to what it was before the last {@link #change()}. Calling a second time switch back to the name before the last {@link #revert()}.
     *
     * @return this
     */
    public ThreadNameStateTool revert() {
        nextName.clear();
        nextName.addAll(previousName);
        change();
        return this;
    }

    public ThreadNameStateTool setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

}