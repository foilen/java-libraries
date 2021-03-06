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
 * To provide a list of items and pagination. You need to extends to enable deserialization on the client side.
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
public abstract class AbstractListResultWithPagination<T> extends AbstractApiBaseWithError {

    private ApiPagination pagination;

    private List<T> items = new ArrayList<>();

    public List<T> getItems() {
        return items;
    }

    public ApiPagination getPagination() {
        return pagination;
    }

    public AbstractListResultWithPagination<T> setItems(List<T> items) {
        this.items = items;
        return this;
    }

    public AbstractListResultWithPagination<T> setPagination(ApiPagination pagination) {
        this.pagination = pagination;
        return this;
    }

}
