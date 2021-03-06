/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A way to provide many items and automatically batch them.
 *
 * Usage:
 *
 * <pre>
 * BufferBatchesTools. &lt;String&gt; autoClose(3, items -> {
 *     batches.add(Joiner.on(",").join(items));
 * }, bufferBatchesTools -> {
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
 * <pre>
 * Dependencies:
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'org.slf4j:slf4j-api:1.7.25'
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

    private int itemsInBatch;

    private Consumer<List<I>> batchExecution;

    private List<I> buffer = new ArrayList<>();

    public BufferBatchesTools(int itemsInBatch, Consumer<List<I>> batchExecution) {
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

        List<I> subList = buffer.subList(0, to);
        batchExecution.accept(subList);
        subList.clear();

    }

}
