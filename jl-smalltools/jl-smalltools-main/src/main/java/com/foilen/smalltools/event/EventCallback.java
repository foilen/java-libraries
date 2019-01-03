/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.event;

/**
 * This is a callback for the {@link EventList}.
 *
 * @param <T>
 *            the type of the parameter passed
 */
public interface EventCallback<T> {

    /**
     * The method that is called when the event happens.
     *
     * @param param
     *            the parameter sent by the event
     */
    void handle(T param);

}
