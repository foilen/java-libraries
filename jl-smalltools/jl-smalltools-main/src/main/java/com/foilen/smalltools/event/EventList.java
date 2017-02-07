/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is to easily register callbacks for event and to manage them.
 *
 * @param <T>
 *            the type of the parameter passed
 */
public class EventList<T> {

    private Queue<EventCallback<T>> callbacks = new ConcurrentLinkedQueue<>();

    /**
     * Register the callback in the list.
     *
     * @param callback
     *            the callback when the event is thrown
     */
    public void addCallback(EventCallback<T> callback) {
        callbacks.add(callback);
    }

    /**
     * Remove all the registered callbacks.
     */
    public void clearCallbacks() {
        callbacks.clear();
    }

    /**
     * Call all the callbacks with the specified parameter.
     *
     * @param param
     *            the param to send
     */
    public void dispatch(T param) {
        for (EventCallback<T> callback : callbacks) {
            callback.handle(param);
        }
    }

    /**
     * Remove the callback from the list.
     *
     * @param callback
     *            the callback when the event is thrown
     */
    public void removeCallback(EventCallback<T> callback) {
        callbacks.remove(callback);
    }

    /**
     * Set the list of callbacks. Useful when using a bean container.
     *
     * @param callbacks
     *            the event callbacks
     */
    public void setCallbacks(Queue<EventCallback<T>> callbacks) {
        this.callbacks = callbacks;
    }

}
