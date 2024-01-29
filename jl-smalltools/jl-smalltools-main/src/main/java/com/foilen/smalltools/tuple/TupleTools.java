/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tuple;

public class TupleTools {

    public static <E> int nullComparator(E o1, E o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (o2 == null) {
                return 1;
            } else {
                if (!(o1 instanceof Comparable)) {
                    throw new IllegalArgumentException("The objects are not comparable");
                }
                return ((Comparable<E>) o1).compareTo(o2);
            }
        }
    }

}
