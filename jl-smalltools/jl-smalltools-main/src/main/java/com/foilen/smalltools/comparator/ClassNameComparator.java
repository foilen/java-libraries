package com.foilen.smalltools.comparator;

import java.util.Comparator;

/**
 * Compare the name of the class in each object.
 */
public class ClassNameComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        return o1.getClass().getName().compareTo(o2.getClass().getName());
    }

}
