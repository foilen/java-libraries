/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.services;

import com.foilen.smalltools.restapi.model.AbstractListResultWithPagination;
import org.springframework.data.domain.Page;

/**
 * Some methods to convert and store items in an {@link AbstractListResultWithPagination}.
 */
public interface PaginationService {

    /**
     * Get the number of items per page.
     *
     * @return the number of items per page
     */
    int getItemsPerPage();

    /**
     * Wrap the results.
     *
     * @param results the results to wrap
     * @param page    the page to get the results from
     * @param apiType the type of the API
     * @param <T>     the type of the API
     */
    <T> void wrap(AbstractListResultWithPagination<T> results, Page<?> page, Class<T> apiType);

    /**
     * Wrap the results.
     *
     * @param results the results to wrap
     * @param page    the page to get the results from
     * @param <T>     the type of the API
     */
    <T> void wrap(AbstractListResultWithPagination<T> results, Page<T> page);

}
