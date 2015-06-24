/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

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
     * @param <T>
     *            the type of the items
     * @return the comparison
     */
    public static <T extends Comparable<T>> List<ListsComparatorDifference<T>> compareLists(List<T> lefts, List<T> rights) {
        int posLeft = 0;
        int posRight = 0;

        List<ListsComparatorDifference<T>> comparisons = new ArrayList<ListsComparatorDifference<T>>();

        while (posLeft < lefts.size() || posRight < rights.size()) {
            T left = null;
            if (posLeft < lefts.size()) {
                left = lefts.get(posLeft);
            }
            T right = null;
            if (posRight < rights.size()) {
                right = rights.get(posRight);
            }

            // If one is null
            if (left == null) {
                ++posRight;
                comparisons.add(new ListsComparatorDifference<T>(right, 1));
                continue;
            }

            if (right == null) {
                ++posLeft;
                comparisons.add(new ListsComparatorDifference<T>(left, -1));
                continue;
            }

            // Compare both
            int comparison = left.compareTo(right);
            if (comparison < 0) {
                ++posLeft;
                comparisons.add(new ListsComparatorDifference<T>(left, -1));
            } else if (comparison > 0) {
                ++posRight;
                comparisons.add(new ListsComparatorDifference<T>(right, 1));
            } else {
                ++posLeft;
                ++posRight;
            }
        }

        return comparisons;
    }

    private ListsComparator() {
    }
}
