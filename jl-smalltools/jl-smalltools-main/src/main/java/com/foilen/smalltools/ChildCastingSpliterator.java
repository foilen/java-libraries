package com.foilen.smalltools;

import com.foilen.smalltools.tools.AbstractBasics;

import java.util.Spliterator;

/**
 * A spliterator that casts the elements to a child class.
 *
 * @param <E> the child class
 * @param <T> the parent class that the current Spliterator has
 */
public class ChildCastingSpliterator<E extends T, T> extends AbstractBasics implements Spliterator<T> {

    private final Spliterator<E> spliterator;
    private final Class<T> clazz;

    public ChildCastingSpliterator(Spliterator<E> spliterator, Class<T> clazz) {
        this.spliterator = spliterator;
        this.clazz = clazz;
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public boolean tryAdvance(java.util.function.Consumer<? super T> action) {
        return spliterator.tryAdvance(e -> action.accept(clazz.cast(e)));
    }

    @Override
    public Spliterator<T> trySplit() {
        Spliterator<E> split = spliterator.trySplit();
        if (split == null) {
            return null;
        }
        return new ChildCastingSpliterator<>(split, clazz);
    }

}