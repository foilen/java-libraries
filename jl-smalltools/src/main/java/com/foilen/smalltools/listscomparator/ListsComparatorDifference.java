/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.listscomparator;

/**
 * A difference made by {@link ListsComparator}.
 * 
 * @param <T>
 *            the type of objects being compared
 */
public class ListsComparatorDifference<T> {

    private T object;

    private int side;

    public ListsComparatorDifference() {
    }

    public ListsComparatorDifference(T object, int side) {
        this.object = object;
        this.side = side;
    }

    /**
     * The object that is different.
     * 
     * @return the object that is different
     */
    public T getObject() {
        return object;
    }

    /**
     * -1 means that this object is only on the left side; 1 means that this object is only on the right side.
     * 
     * @return the side that this object is on
     */
    public int getSide() {
        return side;
    }

}
