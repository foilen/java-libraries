/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class StreamToolsTest {

    @Test
    public void testConcat_1() {
        List<Integer> actual = StreamTools.concat( //
                Arrays.asList(3, 1, 2).stream() //
        ).sorted().collect(Collectors.toList());

        Assert.assertEquals(Arrays.asList(1, 2, 3), actual);
    }

    @Test
    public void testConcat_2() {
        List<Integer> actual = StreamTools.concat( //
                Arrays.asList(3, 1, 5).stream(), //
                Arrays.asList(4, 2).stream() //
        ).sorted().collect(Collectors.toList());

        Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), actual);
    }

    @Test
    public void testConcat_3() {
        List<Integer> actual = StreamTools.concat( //
                Arrays.asList(3, 1, 5).stream(), //
                Arrays.asList(7, 6, 8).stream(), //
                Arrays.asList(4, 2).stream() //
        ).sorted().collect(Collectors.toList());

        Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8), actual);
    }

    @Test
    public void testConcat_5() {
        List<Integer> actual = StreamTools.concat( //
                Arrays.asList(3, 1, 5).stream(), //
                Arrays.asList(7, 6, 8).stream(), //
                Arrays.asList(9, 12, 14, 15).stream(), //
                Arrays.asList(11, 10, 13).stream(), //
                Arrays.asList(4, 2).stream() //
        ).sorted().collect(Collectors.toList());

        Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), actual);
    }

    @Test
    public void testConcat_none() {
        List<Object> actual = StreamTools.concat().sorted().collect(Collectors.toList());

        Assert.assertEquals(0, actual.size());
    }

}
