package com.foilen.smalltools.restapi.model;

import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class ApiPaginationTest {

    @Test
    public void test_empty() {
        ApiPagination actual = new ApiPagination(0, 0, 100, 0);
        AssertTools.assertJsonComparison("ApiPaginationTest-test_empty.json", getClass(), actual);
    }

    @Test
    public void test_first_page() {
        ApiPagination actual = new ApiPagination(0, 3, 100, 250);
        AssertTools.assertJsonComparison("ApiPaginationTest-test_first_page.json", getClass(), actual);
    }

    @Test
    public void test_last_page() {
        ApiPagination actual = new ApiPagination(2, 3, 100, 250);
        AssertTools.assertJsonComparison("ApiPaginationTest-test_last_page.json", getClass(), actual);
    }

    @Test
    public void test_second_page() {
        ApiPagination actual = new ApiPagination(1, 3, 100, 250);
        AssertTools.assertJsonComparison("ApiPaginationTest-test_second_page.json", getClass(), actual);
    }

}
