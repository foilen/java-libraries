/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tuple;

import com.foilen.smalltools.db.TestInt;
import com.foilen.smalltools.test.asserts.AssertTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class TupleTest {

    @Test
    public void testTuple2Sortable() {

        List<Tuple2<String, Integer>> list = new java.util.ArrayList<>();
        list.add(new Tuple2<>("b", 2));
        list.add(new Tuple2<>("a", 10));
        list.add(new Tuple2<>("a", 10));
        list.add(new Tuple2<>("a", 6));
        list.add(new Tuple2<>("c", 1));

        List<Tuple2<String, Integer>> expected = new java.util.ArrayList<>();
        expected.add(new Tuple2<>("a", 6));
        expected.add(new Tuple2<>("a", 10));
        expected.add(new Tuple2<>("b", 2));
        expected.add(new Tuple2<>("c", 1));

        var actual = list.stream().sorted().distinct().collect(Collectors.toList());
        AssertTools.assertJsonComparison(expected, actual);

    }

    @Test
    public void testTuple2NotSortable() {

        List<Tuple2<String, TestInt>> list = new java.util.ArrayList<>();
        list.add(new Tuple2<>("b", new TestInt(2)));
        list.add(new Tuple2<>("a", new TestInt(10)));
        list.add(new Tuple2<>("a", new TestInt(10)));
        list.add(new Tuple2<>("a", new TestInt(6)));
        list.add(new Tuple2<>("c", new TestInt(1)));

        // Expect exception with "The objects are not comparable"
        Assert.assertThrows("The objects are not comparable", IllegalArgumentException.class, () -> {
            list.stream().sorted().distinct().collect(Collectors.toList());
        });

    }

    @Test
    public void testTuple3Sortable() {

        List<Tuple3<String, Integer, Long>> list = new java.util.ArrayList<>();
        list.add(new Tuple3<>("b", 3, 1L));
        list.add(new Tuple3<>("a", 10, 2L));
        list.add(new Tuple3<>("a", 10, 5L));
        list.add(new Tuple3<>("a", 10, 2L));
        list.add(new Tuple3<>("a", 6, 100L));
        list.add(new Tuple3<>("c", 1, 1L));

        List<Tuple3<String, Integer, Long>> expected = new java.util.ArrayList<>();
        expected.add(new Tuple3<>("a", 6, 100L));
        expected.add(new Tuple3<>("a", 10, 2L));
        expected.add(new Tuple3<>("a", 10, 5L));
        expected.add(new Tuple3<>("b", 3, 1L));
        expected.add(new Tuple3<>("c", 1, 1L));

        var actual = list.stream().sorted().distinct().collect(Collectors.toList());
        AssertTools.assertJsonComparison(expected, actual);

    }

    @Test
    public void testTuple3NotSortable() {

        List<Tuple3<String, TestInt, Integer>> list = new java.util.ArrayList<>();
        list.add(new Tuple3<>("b", new TestInt(3), 1));
        list.add(new Tuple3<>("a", new TestInt(10), 1));
        list.add(new Tuple3<>("a", new TestInt(10), 1));
        list.add(new Tuple3<>("a", new TestInt(6), 1));
        list.add(new Tuple3<>("c", new TestInt(1), 1));

        // Expect exception with "The objects are not comparable"
        Assert.assertThrows("The objects are not comparable", IllegalArgumentException.class, () -> {
            list.stream().sorted().distinct().collect(Collectors.toList());
        });

    }

}
