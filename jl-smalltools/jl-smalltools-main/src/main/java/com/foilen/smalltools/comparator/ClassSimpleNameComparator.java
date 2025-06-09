package com.foilen.smalltools.comparator;

import java.util.Comparator;

/**
 * Compare the simple name of the class in each object.
 */
public class ClassSimpleNameComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
    }

}
