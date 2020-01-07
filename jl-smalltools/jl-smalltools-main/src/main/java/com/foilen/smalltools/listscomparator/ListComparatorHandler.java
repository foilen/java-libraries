/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.listscomparator;

/**
 * Extend with what to do with what is different and what is the same.
 *
 * @param <L>
 *            type of the left item
 * @param <R>
 *            type of the right item
 */
public interface ListComparatorHandler<L, R> {

    /**
     * What to do with items present in both lists.
     *
     * @param left
     *            the left item
     * @param right
     *            the right item
     */
    void both(L left, R right);

    /**
     * What to do with items present only in the left side.
     *
     * @param left
     *            the left item
     */
    void leftOnly(L left);

    /**
     * What to do with items present only in the right side.
     *
     * @param right
     *            the right item
     */
    void rightOnly(R right);

}
