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
