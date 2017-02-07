/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.listscomparator;

/**
 * How to compare 2 different items.
 *
 * @param <L>
 *            type of the left item
 * @param <R>
 *            type of the right item
 */
public interface ItemsComparator<L, R> {

    /**
     * Compare both objects.
     *
     * @param left
     *            the left one
     * @param right
     *            the right one
     * @return -1 , 0 or 1
     */
    public int compareTo(L left, R right);

}
