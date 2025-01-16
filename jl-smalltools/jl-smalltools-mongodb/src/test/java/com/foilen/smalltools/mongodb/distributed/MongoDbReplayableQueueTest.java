/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.mongodb.AbstractEmbeddedMongoDbTest;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

public class MongoDbReplayableQueueTest extends AbstractEmbeddedMongoDbTest {

    @Test
    public void testBroadcasting() {

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var queue1 = new MongoDbReplayableQueue<>(String.class, mongoClient, mongoCollection);
        var queue2 = new MongoDbReplayableQueue<>(String.class, mongoClient, mongoCollection);

        // Add from 1
        queue1.add("A");
        queue1.add("B");
        queue1.add("C");
        queue1.addAll(List.of("D", "E", "F"));

        // Size
        Assertions.assertEquals(6, queue1.size());
        Assertions.assertEquals(6, queue2.size());
        Assertions.assertFalse(queue1.isEmpty());
        Assertions.assertFalse(queue2.isEmpty());

        // Read and advance queue1
        Assertions.assertEquals("A", queue1.poll());
        Assertions.assertEquals("B", queue1.poll());
        Assertions.assertEquals("C", queue1.poll());

        // Size
        Assertions.assertEquals(3, queue1.size());
        Assertions.assertEquals(6, queue2.size());
        Assertions.assertFalse(queue1.isEmpty());
        Assertions.assertFalse(queue2.isEmpty());

        // Read and advance queue2
        Assertions.assertEquals("A", queue2.remove());
        Assertions.assertEquals("B", queue2.remove());
        Assertions.assertEquals("C", queue2.remove());
        Assertions.assertEquals("D", queue2.poll());

        // Size
        Assertions.assertEquals(3, queue1.size());
        Assertions.assertEquals(2, queue2.size());
        Assertions.assertFalse(queue1.isEmpty());
        Assertions.assertFalse(queue2.isEmpty());

        // Peek
        Assertions.assertEquals("D", queue1.peek());
        Assertions.assertEquals("E", queue2.peek());

        // Contains D
        Assertions.assertTrue(queue1.contains("D"));
        Assertions.assertFalse(queue2.contains("D"));

        // Size
        Assertions.assertEquals(3, queue1.size());
        Assertions.assertEquals(2, queue2.size());

        // Finish reading queue1
        Assertions.assertEquals("D", queue1.poll());
        Assertions.assertEquals("E", queue1.poll());
        Assertions.assertEquals("F", queue1.poll());

        // Size
        Assertions.assertEquals(0, queue1.size());
        Assertions.assertEquals(2, queue2.size());
        Assertions.assertTrue(queue1.isEmpty());
        Assertions.assertFalse(queue2.isEmpty());

    }

    @Test
    @Timeout(30)
    public void testMultipleProcessingThreads() {

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var producerQueue = new MongoDbReplayableQueue<>(Integer.class, mongoClient, mongoCollection);
        var consumerQueue = new MongoDbReplayableQueue<>(Integer.class, mongoClient, mongoCollection);

        producerQueue.add(-1);
        producerQueue.add(-1);
        producerQueue.add(-1);

        consumerQueue.movePointerToEnd();

        // Consumer
        Deque<Integer> retrieved = new LinkedList<>();
        Runnable consumeRunnable = () -> {
            while (true) {
                try {
                    int value = consumerQueue.take();
                    System.out.println("Got value: " + value);
                    if (value == -1) {
                        break;
                    }
                    retrieved.add(value);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Consumer finished");
        };
        List<Future<?>> futures = new ArrayList<>();
        futures.add(ExecutorsTools.getCachedDaemonThreadPool().submit(consumeRunnable));
        futures.add(ExecutorsTools.getCachedDaemonThreadPool().submit(consumeRunnable));

        // Producer
        for (int i = 0; i < 1000; ++i) {
            producerQueue.add(i);
        }
        producerQueue.add(-1);
        producerQueue.add(-1);

        // Wait for the consumer to finish
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Check
        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i < 1000; ++i) {
            expected.add(i);
        }

        Assertions.assertEquals(expected, retrieved.stream().sorted().toList());

    }
}
