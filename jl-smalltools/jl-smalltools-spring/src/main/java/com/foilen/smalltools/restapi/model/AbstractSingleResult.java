/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

/**
 * To provide a single item. You need to extends to enable deserialization on the client side.
 *
 * @param <T> the type of the returned item
 */
public abstract class AbstractSingleResult<T> extends AbstractApiBaseWithError {

    private T item;

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

}
