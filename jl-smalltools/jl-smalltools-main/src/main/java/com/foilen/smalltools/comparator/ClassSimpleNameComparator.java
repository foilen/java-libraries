/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
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
