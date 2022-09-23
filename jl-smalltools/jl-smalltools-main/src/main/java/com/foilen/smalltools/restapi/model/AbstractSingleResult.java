/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

/**
 * To provide a single item. You need to extends to enable deserialization on the client side.
 *
 * <pre>
 * Dependencies:
 * implementation 'org.apache.commons:commons-lang3:3.12.0'
 * implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.4'
 * implementation 'org.slf4j:slf4j-api:2.0.2'
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
