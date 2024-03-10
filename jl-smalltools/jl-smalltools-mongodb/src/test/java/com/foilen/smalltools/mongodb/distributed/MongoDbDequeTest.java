/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.mongodb.AbstractEmbeddedMongoDbTest;
import com.foilen.smalltools.tools.ExecutorsTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.ThreadTools;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class MongoDbDequeTest extends AbstractEmbeddedMongoDbTest {

    @Test
    public void testSingleThreadAllMethods() {

        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var deque = new MongoDbDeque<>(String.class, mongoClient, mongoCollection);

        // Add in spiral
        Assertions.assertTrue(deque.offerFirst("c"));
        Assertions.assertTrue(deque.offerLast("d"));
        deque.addLast("e");
        Assertions.assertTrue(deque.offerFirst("b"));
        deque.addFirst("a");
        deque.addLast("f");
        Assertions.assertEquals(6, deque.size());

        // Peak one on both ends
        Assertions.assertEquals("a", deque.getFirst());
        Assertions.assertEquals("f", deque.getLast());

        // Poll
        Assertions.assertEquals("a", deque.pollFirst());
        Assertions.assertEquals("f", deque.pollLast());

        // Peak one on both ends
        Assertions.assertEquals("b", deque.peekFirst());
        Assertions.assertEquals("e", deque.peekLast());

        // Remove one on both ends
        Assertions.assertEquals("b", deque.removeFirst());
        Assertions.assertEquals("e", deque.removeLast());

        // Peak one on both ends
        Assertions.assertEquals("c", deque.peekFirst());
        Assertions.assertEquals("d", deque.peekLast());

        Assertions.assertEquals(2, deque.size());
        Assertions.assertFalse(deque.isEmpty());

        // Remove the rest
        deque.clear();
        Assertions.assertEquals(0, deque.size());
        Assertions.assertTrue(deque.isEmpty());

        // Test exceptions
        Assertions.assertThrows(NoSuchElementException.class, deque::getFirst);
        Assertions.assertThrows(NoSuchElementException.class, deque::removeFirst);
        Assertions.assertThrows(NoSuchElementException.class, deque::getLast);
        Assertions.assertThrows(NoSuchElementException.class, deque::removeLast);

        // Test nulls
        Assertions.assertNull(deque.pollFirst());
        Assertions.assertNull(deque.pollLast());
        Assertions.assertNull(deque.peekFirst());
        Assertions.assertNull(deque.peekLast());

        // Insert null
        Assertions.assertThrows(NullPointerException.class, () -> deque.addFirst(null));
        Assertions.assertThrows(NullPointerException.class, () -> deque.addLast(null));

        // Insert many
        List<String> many = new ArrayList<>();
        for (int i = 0; i < 98; ++i) {
            many.add("I" + i);
        }
        deque.addAll(many);
        Assertions.assertEquals(98, deque.size());
        Assertions.assertFalse(deque.isEmpty());

        // Drain 10
        List<String> drained = new ArrayList<>();
        Assertions.assertEquals(10, deque.drainTo(drained, 10));
        Assertions.assertEquals(10, drained.size());
        Assertions.assertEquals(88, deque.size());
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            expected.add("I" + i);
        }
        Assertions.assertEquals(expected, drained);

        // Drain the rest
        drained.clear();
        Assertions.assertEquals(88, deque.drainTo(drained));
        Assertions.assertEquals(88, drained.size());
        Assertions.assertEquals(0, deque.size());
        expected.clear();
        for (int i = 10; i < 98; ++i) {
            expected.add("I" + i);
        }
        Assertions.assertEquals(expected, drained);

        // Test removal
        Assertions.assertTrue(deque.addAll(List.of("e", "a", "d", "Z", "b", "c", "d", "Y", "e", "a", "f")));
        Assertions.assertTrue(deque.removeFirstOccurrence("d"));
        Assertions.assertTrue(deque.remove("e"));
        Assertions.assertTrue(deque.removeLastOccurrence("a"));
        Assertions.assertTrue(deque.removeAll(List.of("Y", "Z")));
        Assertions.assertFalse(deque.remove("1"));
        Assertions.assertFalse(deque.removeFirstOccurrence("1"));
        Assertions.assertFalse(deque.removeLastOccurrence("1"));
        Assertions.assertFalse(deque.removeAll(List.of("1", "2")));
        assertList(deque, "a", "b", "c", "d", "e", "f");

        // Test iterator descending and remove
        Assertions.assertTrue(deque.offerLast("Z"));
        var it = deque.descendingIterator();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("Z", it.next());
        it.remove();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("f", it.next());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("e", it.next());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("d", it.next());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("c", it.next());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("b", it.next());
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals("a", it.next());
        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
        assertList(deque, "a", "b", "c", "d", "e", "f");

        // Test contains
        Assertions.assertTrue(deque.contains("a"));
        Assertions.assertTrue(deque.containsAll(List.of("b", "c", "f")));
        Assertions.assertFalse(deque.contains("1"));
        Assertions.assertFalse(deque.containsAll(List.of("1", "2")));
        Assertions.assertFalse(deque.containsAll(List.of("1", "a")));

        // Test retainAll
        Assertions.assertTrue(deque.retainAll(List.of("c", "b", "f", "d")));
        assertList(deque, "b", "c", "d", "f");

        // Test toArray
        Assertions.assertArrayEquals(new Object[]{"b", "c", "d", "f"}, deque.toArray());

        // Test contains all with some duplicates and some missing
        deque.clear();
        deque.addAll(List.of("a", "a", "b", "c", "d", "e", "f"));
        Assertions.assertTrue(deque.containsAll(List.of("a", "b")));
        Assertions.assertFalse(deque.containsAll(List.of("a", "z")));
        Assertions.assertFalse(deque.containsAll(List.of("a", "a", "z")));
        Assertions.assertTrue(deque.containsAll(List.of("a", "a", "a", "a")));
    }

    @Test
    public void testMultipleThreads_onlyProduce() throws InterruptedException {
        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var deque = getDeque(mongoCollection);

        // Publishers
        int threadPairs = 10;
        CountDownLatch latch = new CountDownLatch(threadPairs * 2);
        for (int thread = 0; thread < threadPairs; ++thread) {
            int finalThread = thread;
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    for (int i = 100 * finalThread; i < (100 * finalThread + 50); ++i) {
                        deque.addLast(i);
                        ThreadTools.sleep(20);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    for (int i = (100 * finalThread + 50); i < (100 * finalThread + 100); ++i) {
                        deque.addFirst(i);
                        ThreadTools.sleep(20);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }

        // Wait for all to finish
        latch.await();

        // Check
        List<Integer> actual = deque.stream()
                .sorted()
                .toList();
        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i < 1000; ++i) {
            expected.add(i);
        }
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testMultipleThreads_onlyConsume() throws InterruptedException {
        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var deque = getDeque(mongoCollection);

        // Insert 1000
        for (int i = 0; i < 1000; ++i) {
            deque.addLast(i);
        }

        // Consumers
        int threadPairs = 10;
        CountDownLatch latch = new CountDownLatch(threadPairs * 2);
        ConcurrentLinkedDeque<Integer> consumed = new ConcurrentLinkedDeque<>();
        for (int thread = 0; thread < threadPairs; ++thread) {
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    for (int i = 0; i < 50; ++i) {
                        consumed.add(deque.takeFirst());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                try {
                    for (int i = 0; i < 50; ++i) {
                        consumed.add(deque.takeLast());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }

        // Wait for all to finish
        latch.await();

        // Check
        var actual = consumed.stream()
                .sorted()
                .toList();
        var expected = new ArrayList<>();
        for (int i = 0; i < 1000; ++i) {
            expected.add(i);
        }
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(deque.isEmpty());

    }

    @Test
    public void testMultipleThreads_produceAndConsume() {
        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);
        var deque = getDeque(mongoCollection);

        // Put some initial
        deque.addAll(List.of(0, 1, 2, 3, 4, 5));

        // Publishers
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            try {
                for (int i = 6; i < 100; ++i) {
                    deque.addLast(i);
                    ThreadTools.sleep(20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            try {
                for (int i = 100; i < 200; ++i) {
                    deque.addLast(i);
                    ThreadTools.sleep(20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Consumers
        Deque<Integer> consumed = new ConcurrentLinkedDeque<>();
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            for (int i = 0; i < 100; ++i) {
                try {
                    consumed.add(deque.takeFirst());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            for (int i = 0; i < 100; ++i) {
                try {
                    consumed.add(deque.takeFirst());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Wait for all to finish
        while (consumed.size() != 200) {
            System.out.println("Waiting for all to finish. Consumed: " + consumed.size());
            ThreadTools.sleep(100);
        }

        // Check
        var actual = consumed.stream()
                .sorted()
                .toList();
        var expected = new ArrayList<>();
        for (int i = 0; i < 200; ++i) {
            expected.add(i);
        }
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(deque.isEmpty());
    }

    @Test
    public void testMultipleProcesses_produceAndConsume() {
        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);

        // Put some initial
        {
            var deque = getDeque(mongoCollection);
            deque.addAll(List.of(0, 1, 2, 3, 4, 5));
        }

        // Publishers
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            try {
                var deque = getDeque(mongoCollection);
                for (int i = 6; i < 100; ++i) {
                    deque.addLast(i);
                    ThreadTools.sleep(20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            try {
                var deque = getDeque(mongoCollection);
                for (int i = 100; i < 200; ++i) {
                    deque.addLast(i);
                    ThreadTools.sleep(20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Consumers
        Deque<Integer> consumed = new ConcurrentLinkedDeque<>();
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            var deque = getDeque(mongoCollection);
            for (int i = 0; i < 100; ++i) {
                try {
                    consumed.add(deque.takeFirst());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
            for (int i = 0; i < 100; ++i) {
                var deque = getDeque(mongoCollection);
                try {
                    consumed.add(deque.takeFirst());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Wait for all to finish
        while (consumed.size() != 200) {
            System.out.println("Waiting for all to finish. Consumed: " + consumed.size());
            ThreadTools.sleep(100);
        }

        // Check
        var actual = consumed.stream()
                .sorted()
                .toList();
        var expected = new ArrayList<>();
        for (int i = 0; i < 200; ++i) {
            expected.add(i);
        }
        Assertions.assertEquals(expected, actual);
        {
            var deque = getDeque(mongoCollection);
            Assertions.assertTrue(deque.isEmpty());
        }
    }

    @Test
    public void testStress() {
        String collectionName = SecureRandomTools.randomHexString(10);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase("test").getCollection(collectionName);

        // Produce a lot, but wait before reading them
        CyclicBarrier barrierSingle = new CyclicBarrier(1000);
        for (int i = 0; i < 500; ++i) { // Separated processes
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                var deque = getDeque(mongoCollection);
                try {
                    barrierSingle.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                deque.addLast(finalI);
            });
        }
        {
            var deque = getDeque(mongoCollection);
            for (int i = 500; i < 1000; ++i) { // Separated threads
                int finalI = i;
                ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                    try {
                        barrierSingle.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    deque.addLast(finalI);
                });
            }
        }

        // Wait for all to finish
        {
            var deque = getDeque(mongoCollection);
            while (deque.size() < 1000) {
                System.out.println("Waiting for all to finish. Got: " + deque.size());
                ThreadTools.sleep(100);
            }
        }

        // Consume them all without waiting
        Deque<Integer> consumed = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < 250; ++i) { // Separated processes
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                var deque = getDeque(mongoCollection);
                try {
                    barrierSingle.await();
                    consumed.add(deque.pollFirst());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                var deque = getDeque(mongoCollection);
                try {
                    barrierSingle.await();
                    consumed.add(deque.pollLast());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
        {
            var deque = getDeque(mongoCollection);
            for (int i = 0; i < 250; ++i) { // Separated threads
                ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                    try {
                        barrierSingle.await();
                        consumed.add(deque.pollFirst());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                    try {
                        barrierSingle.await();
                        consumed.add(deque.pollLast());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        // Wait for all to finish
        {
            var deque = getDeque(mongoCollection);
            while (consumed.size() < 1000) {
                System.out.println("Waiting for all to finish. Got: " + consumed.size());
                ThreadTools.sleep(100);
            }
            Assertions.assertTrue(deque.isEmpty());
        }

        // Check
        var actual = consumed.stream()
                .sorted()
                .toList();
        var expected = new ArrayList<>();
        for (int i = 0; i < 1000; ++i) {
            expected.add(i);
        }
        Assertions.assertEquals(expected, actual);

        // All at the same time
        consumed.clear();
        CyclicBarrier barrierDouble = new CyclicBarrier(2000);
        for (int i = 0; i < 500; ++i) { // Separated processes
            int finalI = i;
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                var deque = getDeque(mongoCollection);
                try {
                    barrierDouble.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                deque.addLast(finalI);
            });
        }
        {
            var deque = getDeque(mongoCollection);
            for (int i = 500; i < 1000; ++i) { // Separated threads
                int finalI = i;
                ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                    try {
                        barrierDouble.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    deque.addLast(finalI);
                });
            }
        }

        for (int i = 0; i < 250; ++i) { // Separated processes
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                var deque = getDeque(mongoCollection);
                try {
                    barrierDouble.await();
                    consumed.add(deque.takeFirst());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                var deque = getDeque(mongoCollection);
                try {
                    barrierDouble.await();
                    consumed.add(deque.takeLast());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
        {
            var deque = getDeque(mongoCollection);
            for (int i = 0; i < 250; ++i) { // Separated threads
                ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                    try {
                        barrierDouble.await();
                        consumed.add(deque.takeFirst());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                ExecutorsTools.getCachedDaemonThreadPool().submit(() -> {
                    try {
                        barrierDouble.await();
                        consumed.add(deque.takeLast());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        // Wait for all to finish
        {
            long endTime = System.currentTimeMillis() + 30000;
            var deque = getDeque(mongoCollection);
            while (consumed.size() < 1000 && endTime > System.currentTimeMillis()) {
                System.out.println("Waiting for all to finish. Got: " + consumed.size() + " still to consume " + deque.size());
                ThreadTools.sleep(100);
            }
        }

        System.out.println("Consumed: " + consumed.size());
        System.out.println("Deque: " + getDeque(mongoCollection).size());

        // Check
        actual = consumed.stream()
                .sorted()
                .toList();
        Assertions.assertEquals(expected, actual);
        {
            var deque = getDeque(mongoCollection);
            Assertions.assertTrue(deque.isEmpty());
        }

    }

    private static MongoDbDeque<Integer> getDeque(MongoCollection<Document> mongoCollection) {
        return new MongoDbDeque<>(Integer.class, mongoClient, mongoCollection, 1000);
    }

    private void assertList(MongoDbDeque<String> deque, String... expected) {
        List<String> expectedList = List.of(expected);
        List<String> actualList = deque.stream().toList();
        Assertions.assertEquals(expectedList, actualList);
    }

}
