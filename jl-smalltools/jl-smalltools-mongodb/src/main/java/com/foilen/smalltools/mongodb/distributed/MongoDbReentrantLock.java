/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed;

import com.foilen.smalltools.mongodb.MongoDbChangeStreamWaitAnyChange;
import com.foilen.smalltools.mongodb.MongoDbManageCollectionTools;
import com.foilen.smalltools.mongodb.distributed.internal.HoldingLockDetails;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tools.ThreadTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * A distributed Locking mechanism that is using MongoDB.
 * <p>
 * When a lock is held, it will send a heartbeat to the database to keep the lock. If the heartbeat is not sent, the lock will be released when the time expire.
 */
public class MongoDbReentrantLock extends AbstractBasics {

    private static final ThreadLocal<String> threadUniqueId = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());

    private final MongoCollection<Document> mongoCollection;
    private final long stopChangeStreamAfterNoThreadWaitedInMs;
    private final long heartbeatIntervalInMs;
    private final long expireLockAfterNoHeartbeatInMs;
    private final long dropLockAfterHeldForTooLongInMs;

    private final ConcurrentMap<String, HoldingLockDetails> holdingByLockName = new ConcurrentHashMap<>();
    private MongoDbChangeStreamWaitAnyChange mongoDbChangeStreamWaitAnyChange;

    private Thread heartbeatThread;

    /**
     * Create a lock using the default values.
     *
     * <ul>
     * <li>After waiting for a lock, the change stream will stop after 10 minutes that no thread is waiting for a lock.</li>
     * <li>The heartbeat will be sent every 30 seconds.</li>
     * <li>If no heartbeat is sent for 1 minute 30 seconds (3 heartbeats), the lock will be released.</li>
     * <li>If the lock is held for 10 minutes, it will be released. That means the longest task execution time with this lock is 10 minutes. It will be released even if there is a heartbeat.</li>
     * </ul>
     *
     * @param mongoClient     the mongo client
     * @param mongoCollection the collection to use
     */
    public MongoDbReentrantLock(MongoClient mongoClient, MongoCollection<Document> mongoCollection) {
        this(mongoClient, mongoCollection,
                10 * 60000, // 10 minutes
                30000, // 30 seconds
                90000, // 1 minute 30 seconds
                600000 // 10 minutes
        );
    }

    /**
     * Create a lock using the provided values.
     *
     * @param mongoClient                             the mongo client
     * @param mongoCollection                         the collection to use
     * @param stopChangeStreamAfterNoThreadWaitedInMs the time to stop the change stream after no thread waited for a lock
     * @param heartbeatIntervalInMs                   the time between each heartbeat
     * @param expireLockAfterNoHeartbeatInMs          the time to expire the lock if no heartbeat is sent (suggest 3 heartbeats)
     * @param dropLockAfterHeldForTooLongInMs         the time to expire the lock if it is held for too long
     */
    public MongoDbReentrantLock(MongoClient mongoClient, MongoCollection<Document> mongoCollection,
                                long stopChangeStreamAfterNoThreadWaitedInMs,
                                long heartbeatIntervalInMs,
                                long expireLockAfterNoHeartbeatInMs,
                                long dropLockAfterHeldForTooLongInMs
    ) {
        this.mongoCollection = mongoCollection;
        this.stopChangeStreamAfterNoThreadWaitedInMs = stopChangeStreamAfterNoThreadWaitedInMs;
        this.heartbeatIntervalInMs = heartbeatIntervalInMs;
        if (expireLockAfterNoHeartbeatInMs <= heartbeatIntervalInMs) {
            throw new IllegalArgumentException("expireLockAfterNoHeartbeatInMs must be at least heartbeatIntervalInMs (suggests 3 heartbeats)");
        }
        this.expireLockAfterNoHeartbeatInMs = expireLockAfterNoHeartbeatInMs;
        this.dropLockAfterHeldForTooLongInMs = dropLockAfterHeldForTooLongInMs;

        MongoDbManageCollectionTools.addCollectionIfMissing(mongoClient, mongoCollection.getNamespace());
        MongoDbManageCollectionTools.manageIndexes(mongoCollection, Map.of(
                "expireAt", new Tuple2<>(
                        new Document().append(MongoDbDistributedConstants.FIELD_EXPIRE_AT, 1),
                        new IndexOptions().expireAfter(0L, TimeUnit.MILLISECONDS)
                )
        ));
    }

    /**
     * Lock the lockName. If the lock is free, it will be locked. If the lock is already held, it will return false.
     * <p>
     * If the lock is already held by the current thread, it will increase its holding count and it will return true.
     *
     * @param lockName the name of the lock
     * @return true if the lock was free and is now locked
     */
    public boolean tryLock(String lockName) {

        // Check if already holding
        String currentThreadUniqueId = threadUniqueId.get();
        var holdingThreadDetails = holdingByLockName.get(lockName);
        if (holdingThreadDetails != null) {
            // By the current thread
            if (StringTools.safeEquals(currentThreadUniqueId, holdingThreadDetails.getThreadUniqueId())) {
                holdingThreadDetails.incrementReentrantCount();
                return true;
            }
            return false;
        }

        // Try to lock from MongoDB
        try {
            mongoCollection.insertOne(new Document()
                    .append(MongoDbDistributedConstants.FIELD_ID, lockName)
                    .append(MongoDbDistributedConstants.FIELD_HOLDING_THREAD_ID, currentThreadUniqueId)
                    .append(MongoDbDistributedConstants.FIELD_EXPIRE_AT, new Date(System.currentTimeMillis() + expireLockAfterNoHeartbeatInMs))
            );
            holdingByLockName.put(lockName, new HoldingLockDetails(currentThreadUniqueId, dropLockAfterHeldForTooLongInMs));
            startHeartbeatThread();
            return true;
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                return false;
            } else {
                logger.error("Unexpected MongoDB error", e);
            }
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }

        return false;
    }

    /**
     * Lock the lockName. If the lock is free, it will be locked. If the lock is already held, it will wait for it to be released for the max wait time.
     * <p>
     * If the lock is already held by the current thread, it will increase its holding count and it will return true.
     *
     * @param lockName the name of the lock
     * @param timeInMs the max time to wait
     * @return true if the lock was free and is now locked
     * @throws InterruptedException if the thread was interrupted while waiting
     */
    public boolean tryLock(String lockName, long timeInMs) throws InterruptedException {

        if (tryLock(lockName)) {
            return true;
        }

        long waitUntil = System.currentTimeMillis() + timeInMs;
        if (waitUntil < 0) {
            waitUntil = Long.MAX_VALUE;
        }

        boolean gotLock = false;
        while (!gotLock && System.currentTimeMillis() < waitUntil) {
            waitForChange(lockName, waitUntil - System.currentTimeMillis());
            gotLock = tryLock(lockName);
        }

        return gotLock;
    }

    private void waitForChange(String lockName, long timeInMs) throws InterruptedException {
        synchronized (this) {
            if (mongoDbChangeStreamWaitAnyChange == null) {
                mongoDbChangeStreamWaitAnyChange = new MongoDbChangeStreamWaitAnyChange(mongoCollection, stopChangeStreamAfterNoThreadWaitedInMs, "delete");
            }
        }
        mongoDbChangeStreamWaitAnyChange.waitForChange(lockName, timeInMs);
    }

    /**
     * Unlock the lockName.
     * <p>
     * Must be called the same amount of time as the lock was called and by the same thread.
     *
     * @param lockName the name of the lock
     */
    public void unlock(String lockName) {

        // Check if holding
        HoldingLockDetails holdingThreadDetails = holdingByLockName.get(lockName);
        if (holdingThreadDetails == null) {
            return;
        }

        // By the current thread
        String currentThreadUniqueId = threadUniqueId.get();
        if (!StringTools.safeEquals(currentThreadUniqueId, holdingThreadDetails.getThreadUniqueId())) {
            return;
        }

        // Decrease the reentrant count
        if (holdingThreadDetails.decrementReentrantCount() > 0) {
            return;
        }

        logger.debug("Unlocking {}", lockName);

        // Remove from the map
        holdingByLockName.remove(lockName);

        // Remove from MongoDB
        mongoCollection.deleteOne(new Document().append(MongoDbDistributedConstants.FIELD_ID, lockName));

    }

    // ---== Methods reusing the others at the top  ==---

    /**
     * Lock the lockName. If the lock is already held, it will wait for it to be released.
     *
     * @param lockName the name of the lock
     * @throws InterruptedException if the thread was interrupted while waiting
     */
    public void lock(String lockName) throws InterruptedException {
        tryLock(lockName, Long.MAX_VALUE);
    }

    /**
     * Get the lock if available, execute the code and then release the lock. If not available, just skip.
     *
     * @param lockName the name of the lock
     * @param runnable the code to execute when the lock is held
     * @return true if it could get the lock and execute the code
     */
    public boolean noWaitLockAndExecute(String lockName, Runnable runnable) {
        boolean gotLock = tryLock(lockName);
        if (gotLock) {
            try {
                runnable.run();
            } finally {
                unlock(lockName);
            }
        }
        return gotLock;
    }

    /**
     * Wait to get the lock, execute the code and then release the lock.
     *
     * @param lockName the name of the lock
     * @param runnable the code to execute when the lock is held
     * @throws InterruptedException if the thread was interrupted while waiting
     */
    public void waitLockAndExecute(String lockName, Runnable runnable) throws InterruptedException {
        lock(lockName);
        try {
            runnable.run();
        } finally {
            unlock(lockName);
        }
    }

    /**
     * Wait to get the lock (for the maximum wait time), execute the code and then release the lock.
     *
     * @param lockName the name of the lock
     * @param timeInMs the max time to wait
     * @param runnable the code to execute when the lock is held
     * @return true if it could get the lock and execute the code
     * @throws InterruptedException if the thread was interrupted while waiting
     */
    public boolean waitLockAndExecute(String lockName, long timeInMs, Runnable runnable) throws InterruptedException {
        boolean gotLock = tryLock(lockName, timeInMs);
        if (gotLock) {
            try {
                runnable.run();
            } finally {
                unlock(lockName);
            }
        }
        return gotLock;
    }

    private void startHeartbeatThread() {
        var reentrantLock = this;
        synchronized (reentrantLock) {
            if (heartbeatThread == null) {
                heartbeatThread = new Thread(() -> {
                    logger.info("Starting heartbeat thread");
                    while (true) {
                        ThreadTools.sleep(heartbeatIntervalInMs);

                        // Check if needs to stop
                        if (holdingByLockName.isEmpty()) {
                            synchronized (reentrantLock) {
                                holdingByLockName.isEmpty();
                            }
                            logger.info("Stopping heartbeat thread");
                            return;
                        }

                        // Send
                        try {
                            sendHeartbeats();
                        } catch (Exception e) {
                            logger.error("Problem sending heartbeats", e);
                        }
                    }
                }, "Lock heartbeat for " + mongoCollection.getNamespace() + "-" + SecureRandomTools.randomHexString(5));
                heartbeatThread.setDaemon(true);
                heartbeatThread.start();
            }
        }

    }

    private void sendHeartbeats() {
        holdingByLockName.keySet().forEach(lockName -> {
            logger.debug("Sending heartbeat for {}", lockName);
            try {
                var holdingThreadDetails = holdingByLockName.get(lockName);
                var result = mongoCollection.updateOne(new Document()
                                .append(MongoDbDistributedConstants.FIELD_ID, lockName)
                                .append(MongoDbDistributedConstants.FIELD_HOLDING_THREAD_ID, holdingThreadDetails.getThreadUniqueId()),
                        new Document().append("$set", new Document()
                                .append(MongoDbDistributedConstants.FIELD_EXPIRE_AT, new Date(System.currentTimeMillis() + expireLockAfterNoHeartbeatInMs))
                        ));
                if (result.getModifiedCount() == 0) {
                    logger.error("Lost the lock {}", lockName);
                    holdingByLockName.remove(lockName);
                }
            } catch (Exception e) {
                logger.error("Problem sending heartbeat for " + lockName, e);
            }
        });
    }

}
