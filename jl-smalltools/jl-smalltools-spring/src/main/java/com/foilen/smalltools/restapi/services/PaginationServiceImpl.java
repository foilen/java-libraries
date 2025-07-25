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
