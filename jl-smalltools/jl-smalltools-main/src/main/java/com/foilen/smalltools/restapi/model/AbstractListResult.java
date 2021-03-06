/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * To provide a list of items. You need to extends to enable deserialization on the client side. You can add an {@link ApiPagination} if needed.
 *
 * <pre>
 * Dependencies:
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 *
 * @param <T>
 *            the type of the returned items
 */
public abstract class AbstractListResult<T> extends AbstractApiBaseWithError {

    private List<T> items = new ArrayList<>();

    public List<T> getItems() {
        return items;
    }

    public AbstractListResult<T> setItems(List<T> items) {
        this.items = items;
        return this;
    }

}
