/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.executor;

import com.foilen.smalltools.ChildCastingSpliterator;
import com.foilen.smalltools.iterator.ChildCastingIterator;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PriorityRunnableBlockingQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {

    private final ThreadLocal<Long> priority = ThreadLocal.withInitial(System::currentTimeMillis);
    private final PriorityBlockingQueue<PriorityRunnable> queue = new PriorityBlockingQueue<>();

    /**
     * Set the priority on the current thread.
     *
     * @param priority The priority. Smaller number is higher priority. Long.MIN_VALUE is the highest priority. Long.MAX_VALUE is the lowest priority.
     */
    public void setPriorityOnThread(long priority) {
        this.priority.set(priority);
    }

    private PriorityRunnable wrap(Runnable runnable) {
        return new PriorityRunnable(priority.get(), runnable);
    }

    @Override
    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    @Override
    public boolean add(Runnable runnable) {
        return queue.add(wrap(runnable));
    }

    @Override
    public boolean offer(Runnable runnable) {
        return queue.offer(wrap(runnable));
    }

    @Override
    public void put(Runnable runnable) {
        queue.put(wrap(runnable));
    }

    @Override
    public boolean offer(Runnable runnable, long timeout, TimeUnit unit) {
        return queue.offer(wrap(runnable), timeout, unit);
    }

    @Override
    public Runnable poll() {
        return queue.poll();
    }

    @Override
    public Runnable take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public Runnable peek() {
        return queue.peek();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return queue.contains(o);
    }

    @Override
    public String toString() {
        return queue.toString();
    }

    @Override
    public int drainTo(Collection<? super Runnable> c) {
        return queue.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super Runnable> c, int maxElements) {
        return queue.drainTo(c, maxElements);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return queue.toArray(a);
    }

    @Override
    public Iterator<Runnable> iterator() {
        return new ChildCastingIterator<>(queue.iterator(), Runnable.class);
    }

    @Override
    public Spliterator<Runnable> spliterator() {
        return new ChildCastingSpliterator<>(queue.spliterator(), Runnable.class);
    }

    public boolean removeIf(Predicate<? super Runnable> filter) {
        return queue.removeIf(filter);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return queue.retainAll(c);
    }

    public void forEach(Consumer<? super Runnable> action) {
        queue.forEach(action);
    }

    @Override
    public Runnable remove() {
        return queue.remove();
    }

    @Override
    public Runnable element() {
        return queue.element();
    }

    @Override
    public boolean addAll(Collection<? extends Runnable> c) {
        return queue.addAll(c.stream()
                .map(this::wrap)
                .toList()
        );
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return queue.toArray(generator);
    }

    @Override
    public boolean equals(Object o) {
        return queue.equals(o);
    }

    @Override
    public int hashCode() {
        return queue.hashCode();
    }

    @Override
    public Stream<Runnable> stream() {
        return queue.stream().map(PriorityRunnable::getRunnable);
    }

    @Override
    public Stream<Runnable> parallelStream() {
        return queue.parallelStream().map(PriorityRunnable::getRunnable);
    }

}
