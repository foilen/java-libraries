package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.mongodb.AbstractEmbeddedMongoDbTest;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.ThreadTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

    @Test
    @Timeout(60)
    public void testCollectionDroppedWhileUsing() throws InterruptedException {

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);

        // Create instance (creates collection and indexes)
        var queue = new MongoDbReplayableQueue<>(String.class, mongoClient, mongoCollection);

        // Use it normally
        queue.add("item1");
        queue.add("item2");
        queue.add("item3");
        Assertions.assertEquals(3, queue.size());
        Assertions.assertEquals("item1", queue.poll());
        Assertions.assertEquals("item2", queue.poll());
        Assertions.assertEquals("item3", queue.poll());

        // Poll for 15 seconds in another thread
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> polledItem = new AtomicReference<>();
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            try {
                latch.countDown();
                polledItem.set(queue.poll(15, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Drop the collection
        latch.await();
        ThreadTools.sleep(1000);
        mongoCollection.drop();

        // After some time, add an item
        ThreadTools.sleep(2000);
        queue.add("itemNew");
        // Wait for the polled item to be set
        ThreadTools.sleep(5000);

        Assertions.assertEquals("itemNew", polledItem.get());

    }
}
