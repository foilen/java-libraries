/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.listscomparator;

import java.util.ArrayList;
import java.util.List;

/**
 * Some methods to compare lists and retrieve their differences.
 */
public final class ListsComparator {

    /**
     * Compare two ordered lists for differences.
     *
     * @param lefts
     *            ordered list
     * @param rights
     *            ordered list
     * @param itemsComparator
     *            the way to compare both items
     * @param listComparatorHandler
     *            the change handler
     * @param <L>
     *            the type of the left items
     * @param <R>
     *            the type of the right items
     */
    public static <L, R> void compareLists(List<L> lefts, List<R> rights, ItemsComparator<L, R> itemsComparator, ListComparatorHandler<L, R> listComparatorHandler) {
        int posLeft = 0;
        int posRight = 0;

        while (posLeft < lefts.size() || posRight < rights.size()) {
            L left = null;
            if (posLeft < lefts.size()) {
                left = lefts.get(posLeft);
            }
            R right = null;
            if (posRight < rights.size()) {
                right = rights.get(posRight);
            }

            // If one is null
            if (left == null) {
                ++posRight;
                listComparatorHandler.rightOnly(right);
                continue;
            }

            if (right == null) {
                ++posLeft;
                listComparatorHandler.leftOnly(left);
                continue;
            }

            // Compare both
            int comparison = itemsComparator.compareTo(left, right);
            if (comparison < 0) {
                ++posLeft;
                listComparatorHandler.leftOnly(left);
            } else if (comparison > 0) {
                ++posRight;
                listComparatorHandler.rightOnly(right);
            } else {
                listComparatorHandler.both(left, right);
                ++posLeft;
                ++posRight;
            }
        }

    }

    /**
     * Compare two ordered lists for differences.
     *
     * @param lefts
     *            ordered list
     * @param rights
     *            ordered list
     * @param <T>
     *            the type of the items
     * @return the comparison
     */
    public static <T extends Comparable<T>> List<ListsComparatorDifference<T>> compareLists(List<T> lefts, List<T> rights) {
        final List<ListsComparatorDifference<T>> comparisons = new ArrayList<ListsComparatorDifference<T>>();

        compareLists(lefts, rights, new ListComparatorHandler<T, T>() {
            @Override
            public void both(T left, T right) {
            }

            @Override
            public void leftOnly(T left) {
                comparisons.add(new ListsComparatorDifference<T>(left, -1));
            }

            @Override
            public void rightOnly(T right) {
                comparisons.add(new ListsComparatorDifference<T>(right, 1));
            }
        });

        return comparisons;
    }

    /**
     * Compare two ordered lists for differences.
     *
     * @param lefts
     *            ordered list
     * @param rights
     *            ordered list
     * @param listComparatorHandler
     *            the change handler
     * @param <T>
     *            the type of the items
     */
    public static <T extends Comparable<T>> void compareLists(List<T> lefts, List<T> rights, ListComparatorHandler<T, T> listComparatorHandler) {
        compareLists(lefts, rights, (left, right) -> left.compareTo(right), listComparatorHandler);
    }

    private ListsComparator() {
    }
}
