/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.services;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;

import com.foilen.smalltools.restapi.model.AbstractListResultWithPagination;
import com.foilen.smalltools.restapi.model.ApiPagination;
import com.foilen.smalltools.tools.JsonTools;

/**
 * Some methods to convert and store items in an {@link AbstractListResultWithPagination}.
 *
 * <pre>
 * Dependencies:
 * compile "org.springframework:spring-core:4.3.11.RELEASE"
 * compile "org.springframework.data:spring-data-commons:1.13.7.RELEASE"
 * </pre>
 */
public class PaginationServiceImpl implements PaginationService {

    @Autowired
    private ConversionService conversionService;

    private int itemsPerPage = 100;

    public ConversionService getConversionService() {
        return conversionService;
    }

    @Override
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public PaginationServiceImpl setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
        return this;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    @Override
    public <T> void wrap(AbstractListResultWithPagination<T> results, Page<?> page, Class<T> apiType) {
        results.setPagination(new ApiPagination(page));
        if (page.getSize() == 0) {
            results.setItems(Collections.emptyList());
        } else {
            if (conversionService.canConvert(page.getContent().get(0).getClass(), apiType)) {
                results.setItems(page.getContent().stream().map(i -> conversionService.convert(i, apiType)).collect(Collectors.toList()));
            } else {
                results.setItems(page.getContent().stream().map(i -> JsonTools.clone(i, apiType)).collect(Collectors.toList()));
            }
        }
    }

    @Override
    public <T> void wrap(AbstractListResultWithPagination<T> results, Page<T> page) {
        results.setPagination(new ApiPagination(page));
        results.setItems(page.getContent());
    }

}
