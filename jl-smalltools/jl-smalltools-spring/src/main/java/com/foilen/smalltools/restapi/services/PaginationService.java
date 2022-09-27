/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.services;

import org.springframework.data.domain.Page;

import com.foilen.smalltools.restapi.model.AbstractListResultWithPagination;

/**
 * Some methods to convert and store items in an {@link AbstractListResultWithPagination}.
 */
public interface PaginationService {

    int getItemsPerPage();

    <T> void wrap(AbstractListResultWithPagination<T> results, Page<?> page, Class<T> apiType);

    <T> void wrap(AbstractListResultWithPagination<T> results, Page<T> page);

}
