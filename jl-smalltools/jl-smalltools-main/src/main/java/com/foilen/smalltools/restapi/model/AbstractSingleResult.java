/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

/**
 * To provide a single item. You need to extends to enable deserialization on the client side.
 *
 * <pre>
 * Dependencies:
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 *
 * @param <T>
 *            the type of the returned item
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
