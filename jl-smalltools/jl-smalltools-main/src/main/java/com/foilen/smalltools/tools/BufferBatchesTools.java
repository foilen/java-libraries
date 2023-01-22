/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A way to provide many items and automatically batch them.
 *
 * Usage:
 *
 * <pre>
 * BufferBatchesTools. &lt;String&gt; autoClose(3, items -&gt; {
 *     batches.add(Joiner.on(",").join(items));
 * }, bufferBatchesTools -&gt; {
 *     bufferBatchesTools.add("1");
 *     bufferBatchesTools.add("2");
 *     bufferBatchesTools.add("3");
 *     bufferBatchesTools.add("4");
 *     bufferBatchesTools.add(Arrays.asList("5", "6", "7", "8", "9", "10"));
 * });
 *
 * // Gives
 * "1,2,3"
 * "4,5,6"
 * "7,8,9"
 * "10"
 * </pre>
 *
 * @param <I>
 *            the type of item
 */
public class BufferBatchesTools<I> extends AbstractBasics implements Closeable {

    public static <I> void autoClose(int itemsInBatch, Consumer<List<I>> batchExecution, Consumer<BufferBatchesTools<I>> execution) {
        BufferBatchesTools<I> bufferBatchesTools = new BufferBatchesTools<>(itemsInBatch, batchExecution);
        execution.accept(bufferBatchesTools);
        bufferBatchesTools.close();
    }

    public static <I> void autoClose(Collection<I> buffer, int itemsInBatch, Consumer<List<I>> batchExecution, Consumer<BufferBatchesTools<I>> execution) {
        BufferBatchesTools<I> bufferBatchesTools = new BufferBatchesTools<>(buffer, itemsInBatch, batchExecution);
        execution.accept(bufferBatchesTools);
        bufferBatchesTools.close();
    }

    private int itemsInBatch;

    private Consumer<List<I>> batchExecution;

    private Collection<I> buffer = new ArrayList<>();

    public BufferBatchesTools(int itemsInBatch, Consumer<List<I>> batchExecution) {
        this.itemsInBatch = itemsInBatch;
        this.batchExecution = batchExecution;
    }

    public BufferBatchesTools(Collection<I> buffer, int itemsInBatch, Consumer<List<I>> batchExecution) {
        this.buffer = buffer;
        this.itemsInBatch = itemsInBatch;
        this.batchExecution = batchExecution;
    }

    public void add(I item) {
        buffer.add(item);
        while (buffer.size() >= itemsInBatch) {
            process();
        }
    }

    public void add(List<I> items) {
        buffer.addAll(items);
        while (buffer.size() >= itemsInBatch) {
            process();
        }
    }

    @Override
    public void close() {
        while (!buffer.isEmpty()) {
            process();
        }
    }

    private void process() {
        int to = Math.min(itemsInBatch, buffer.size());

        List<I> subList = buffer.stream().limit(to).collect(Collectors.toList());
        batchExecution.accept(subList);
        buffer.removeAll(subList);

    }

}
