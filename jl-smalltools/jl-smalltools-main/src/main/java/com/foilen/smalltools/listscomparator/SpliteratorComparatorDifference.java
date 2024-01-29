/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.listscomparator;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.ExecutorsTools;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A spliterator that will compare two streams and return the differences.
 *
 * @param <T> the type of the objects
 */
public class SpliteratorComparatorDifference<T extends Comparable<T>> extends AbstractBasics implements Spliterator<ListsComparatorDifference<T>>, Runnable {

    private BlockingQueue<ListsComparatorDifference<T>> queue = new ArrayBlockingQueue<>(50);
    private volatile boolean completed = false;
    private Semaphore semaphore = new Semaphore(0);
    private volatile SmallToolsException exception;

    private Stream<T> lefts;
    private Stream<T> rights;

    /**
     * Constructor.
     *
     * @param lefts  the left stream
     * @param rights the right stream
     */
    public SpliteratorComparatorDifference(Stream<T> lefts, Stream<T> rights) {
        this.lefts = lefts;
        this.rights = rights;
        ExecutorsTools.getCachedDaemonThreadPool().execute(this);
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.SORTED;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public Comparator<? super ListsComparatorDifference<T>> getComparator() {
        return (a, b) -> a.getObject().compareTo(b.getObject());
    }

    @Override
    public void run() {

        try {
            // Get next items until the end
            ListsComparator.compareStreams(lefts, rights, new ListComparatorHandler<T, T>() {
                @Override
                public void both(T left, T right) {
                }

                @Override
                public void leftOnly(T left) {
                    try {
                        queue.put(new ListsComparatorDifference<T>(left, -1));
                        semaphore.release();
                    } catch (InterruptedException e) {
                        throw new SmallToolsException(e);
                    }
                }

                @Override
                public void rightOnly(T right) {
                    try {
                        queue.put(new ListsComparatorDifference<T>(right, 1));
                        semaphore.release();
                    } catch (InterruptedException e) {
                        throw new SmallToolsException(e);
                    }
                }
            });

            completed = true;
        } catch (SmallToolsException e) {
            exception = e;
        } catch (Throwable e) {
            exception = new SmallToolsException(e);
        }

        semaphore.release();

    }

    @Override
    public boolean tryAdvance(Consumer<? super ListsComparatorDifference<T>> action) {
        try {
            semaphore.acquire();

            ListsComparatorDifference<T> difference = queue.poll();
            if (difference != null) {
                action.accept(difference);
                return true;
            }

            if (completed == true) {
                return false;
            }

            if (exception != null) {
                throw exception;
            }

        } catch (InterruptedException e) {
            throw new SmallToolsException(e);
        }

        throw new SmallToolsException("Unexpected state");
    }

    @Override
    public Spliterator<ListsComparatorDifference<T>> trySplit() {
        return null;
    }

}
