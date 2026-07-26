package com.foilen.smalltools.restapi.services;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.foilen.smalltools.restapi.model.AbstractListResultWithPagination;

public class PaginationServiceImplTest {

    private static class ListResult extends AbstractListResultWithPagination<String> {
    }

    @Test
    public void test_wrap_emptyPage_withRequestedPageSize() {
        PaginationServiceImpl service = new PaginationServiceImpl();

        // The page is empty, but the requested page size (100) is not 0
        PageImpl<String> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 100), 0);

        ListResult results = new ListResult();
        Assertions.assertDoesNotThrow(() -> service.wrap(results, page, String.class));

        Assertions.assertEquals(Collections.emptyList(), results.getItems());
    }

    @Test
    public void test_wrap_nonEmptyPage() {
        PaginationServiceImpl service = new PaginationServiceImpl();

        service.setConversionService(new DefaultConversionService());

        List<String> content = List.of("a", "b");
        PageImpl<String> page = new PageImpl<>(content, PageRequest.of(0, 100), 2);

        ListResult results = new ListResult();
        service.wrap(results, page, String.class);

        Assertions.assertEquals(content, results.getItems());
    }

}
