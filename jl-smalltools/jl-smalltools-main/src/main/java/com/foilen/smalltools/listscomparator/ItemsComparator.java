package com.foilen.smalltools.listscomparator;

/**
 * How to compare 2 different items.
 *
 * @param <L> type of the left item
 * @param <R> type of the right item
 */
public interface ItemsComparator<L, R> {

    /**
     * Compare both objects.
     *
     * @param left  the left one
     * @param right the right one
     * @return -1 , 0 or 1
     */
    public int compareTo(L left, R right);

}
