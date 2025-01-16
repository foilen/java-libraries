/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.iterator;

import com.foilen.smalltools.tools.AbstractBasics;

import java.util.Iterator;

/**
 * A iterator that casts the elements to a child class.
 *
 * @param <E> the child class
 * @param <T> the parent class that the current Iterator has
 */
public class ChildCastingIterator<E extends T, T> extends AbstractBasics implements Iterator<T> {

    private final Iterator<E> iterator;
    private final Class<T> clazz;

    public ChildCastingIterator(Iterator<E> iterator, Class<T> clazz) {
        this.iterator = iterator;
        this.clazz = clazz;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return clazz.cast(iterator.next());
    }

}
