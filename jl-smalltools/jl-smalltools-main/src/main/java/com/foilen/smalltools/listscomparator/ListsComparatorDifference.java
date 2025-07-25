package com.foilen.smalltools.listscomparator;

/**
 * A difference made by {@link ListsComparator}.
 *
 * @param <T> the type of objects being compared
 */
public class ListsComparatorDifference<T> {

    private T object;

    private int side;

    /**
     * Default constructor.
     */
    public ListsComparatorDifference() {
    }

    /**
     * Full constructor.
     *
     * @param object the object that is different
     * @param side   -1 means that this object is only on the left side; 1 means that this object is only on the right side
     */
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
