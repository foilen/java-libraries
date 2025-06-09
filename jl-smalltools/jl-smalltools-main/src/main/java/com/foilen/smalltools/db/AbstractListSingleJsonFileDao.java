package com.foilen.smalltools.db;

import com.foilen.smalltools.hash.HashMd5sum;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.trigger.SmoothTrigger;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * This is a simple DAO that can contain a list of entities and they will be persisted to a single json file. Whenever a modification method is called, it will request the save of the file, but it
 * won't be saved right away to prevent over-usage of the disk. It will save to the file 2 seconds after the last method call that made a modification or maximum 10 seconds after the first method call
 * that made a modification.
 * </p>
 *
 * <p>
 * Everytime you retrieve an entity, it will be a clone from the underlying entity. That means you can safely modify a saved or retrieved entity without worrying that it will be modifying the same
 * entity in the database or be modified by any other thread that retrieved it. You need to call {@link #update(Object, Object)} to persist any changes.
 * </p>
 *
 * <p>
 * This DAO is not meant for heavy performance, but for low usage and low amount of entities.
 * </p>
 *
 * <p>
 * Every actions are done in a cached in-memory list of entities to ensure to not over-use the disk for reading.
 * </p>
 * <p>
 * Usage:
 * <ol>
 * <li>Create your entity</li>
 * <li>Create a DAO for your entity to persist</li>
 * <li>Instantiate the DAO</li>
 * <li>Use it</li>
 * </ol>
 *
 * <pre>
 * // Create your entity
 * public class TestDbEntity {
 *
 *     private String id;
 *     private int number;
 *
 *     public TestDbEntity() {
 *     }
 *
 *     public TestDbEntity(String id, int number) {
 *         this.id = id;
 *         this.number = number;
 *     }
 *
 *     public String getId() {
 *         return id;
 *     }
 *
 *     public int getNumber() {
 *         return number;
 *     }
 *
 *     public void setId(String id) {
 *         this.id = id;
 *     }
 *
 *     public void setNumber(int number) {
 *         this.number = number;
 *     }
 * }
 *
 * // Create a DAO for your entity to persist
 * public static class TestListSingleDao extends AbstractListSingleJsonFileDao&lt;TestDbEntity, String&gt; {
 *
 *     private File dbFile;
 *     private File stagingFile;
 *
 *     public TestListSingleDao(File dbFile) {
 *         this.dbFile = dbFile;
 *         this.stagingFile = new File(dbFile.getAbsolutePath() + "_tmp");
 *     }
 *
 *     &#64;Override
 *     protected File getFinalFile() {
 *         return dbFile;
 *     }
 *
 *     &#64;Override
 *     protected File getStagingFile() {
 *         return stagingFile;
 *     }
 *
 *     &#64;Override
 *     protected Class&lt;TestDbEntity&gt; getType() {
 *         return TestDbEntity.class;
 *     }
 *
 *     &#64;Override
 *     protected boolean isEntity(String key, TestDbEntity entity) {
 *         return StringTools.safeEquals(key, entity.getId());
 *     }
 * }
 *
 * // Instantiate the DAO
 * TestListSingleDao dao = new TestListSingleDao(dbFile);
 * dao.init(); // Called automatically if DAO is exposed as a Spring Bean
 *
 * // Use it
 * dao.add(new TestDbEntity("id1", 1));
 * dao.add(new TestDbEntity("id2", 2));
 *
 * Optional&lt;TestDbEntity&gt; optional = dao.findOne("id2");
 *
 * List&lt;String&gt; ids = dao.findAllAsStream(it -&gt; it.getNumber() &gt;= 2).map(it -&gt; it.getId()).collect(Collectors.toList());
 * </pre>
 */
public abstract class AbstractListSingleJsonFileDao<T, K> extends AbstractBasics {

    private String previousMd5sum;
    private List<T> cachedEntities;

    /**
     * What to do when needing to save to the file.
     */
    protected Runnable saveToFile = () -> {

        String cachedMd5sum = HashMd5sum.hashString(JsonTools.prettyPrint(cachedEntities));

        // Check if content is different
        if (StringTools.safeEquals(previousMd5sum, cachedMd5sum)) {
            logger.debug("Content didn't change. Skipping saving");
            return;
        }

        // Save
        logger.debug("Saving to {}", getFinalFile().getAbsolutePath());
        OutputStream out = FileTools.createStagingFile(getStagingFile(), getFinalFile());
        JsonTools.writeToStream(out, cachedEntities);
        try {
            out.close();
            previousMd5sum = cachedMd5sum;
        } catch (IOException e) {
            logger.error("Could not close the staging file {}", getStagingFile().getAbsolutePath(), e);
        }

    };

    private SmoothTrigger saveSmoothTrigger = new SmoothTrigger(2000, 10000, false, saveToFile).start();

    /**
     * Add multiple entities. Could add an item with the same key as another.
     *
     * @param entities the entities
     */
    public synchronized void add(Iterable<T> entities) {
        entities.forEach(it -> add(it));
    }

    /**
     * Add an entity. Could add an item with the same key as another.
     *
     * @param entity the entity
     */
    public synchronized void add(T entity) {
        cachedEntities.add(JsonTools.clone(entity));
        saveSmoothTrigger.request();
    }

    /**
     * Gives the amount of persisted entities.
     *
     * @return the count
     */
    public synchronized int count() {
        return cachedEntities.size();
    }

    /**
     * Delete all the entities with the specified key.
     *
     * @param key the key
     * @return true if at least one was deleted
     */
    public synchronized boolean delete(K key) {
        return delete(entity -> isEntity(key, entity)) > 0;
    }

    /**
     * Delete some entities.
     *
     * @param predicate a function that returns true when the entity must be deleted.
     * @return the deleted count
     */
    public synchronized int delete(Predicate<? super T> predicate) {
        int count = 0;

        Iterator<T> it = cachedEntities.iterator();
        while (it.hasNext()) {
            T next = it.next();
            if (predicate.test(next)) {
                ++count;
                it.remove();
            }
        }

        if (count > 0) {
            saveSmoothTrigger.request();
        }

        return count;
    }

    /**
     * Find all entities. All returned entities are clones (modifying them won't change their values in this db).
     *
     * @return the list
     */
    public List<T> findAllAsList() {
        return findAllAsStream().collect(Collectors.toList());
    }

    /**
     * Find all entities that satisfies the predicate. All returned entities are clones (modifying them won't change their values in this db).
     *
     * @param predicate the predicate
     * @return the list
     */
    public synchronized List<T> findAllAsList(Predicate<? super T> predicate) {
        return findAllAsStream(predicate).collect(Collectors.toList());
    }

    /**
     * Find all entities. All returned entities are clones (modifying them won't change their values in this db).
     *
     * @return the stream
     */
    public synchronized Stream<T> findAllAsStream() {
        return cachedEntities.stream().map(it -> JsonTools.clone(it));
    }

    /**
     * Find all entities that satisfies the predicate. All returned entities are clones (modifying them won't change their values in this db).
     *
     * @param predicate the predicate
     * @return the stream
     */
    public synchronized Stream<T> findAllAsStream(Predicate<? super T> predicate) {
        return cachedEntities.stream().filter(predicate).map(it -> JsonTools.clone(it));
    }

    /**
     * Find one entity that is of the specified key. The returned entity is a clone (modifying it won't change its value in this db).
     *
     * @param key the key
     * @return the entity
     */
    public synchronized Optional<T> findOne(K key) {
        return findOne(entity -> isEntity(key, entity));
    }

    /**
     * Find one entity that satisfies the predicate. The returned entity is a clone (modifying it won't change its value in this db).
     *
     * @param predicate the predicate
     * @return the entity
     */
    public synchronized Optional<T> findOne(Predicate<? super T> predicate) {
        return cachedEntities.stream().filter(predicate).map(it -> JsonTools.clone(it)).findAny();
    }

    /**
     * Save to the file now.
     */
    public synchronized void flush() {
        saveToFile.run();
    }

    /**
     * Tell which file will contain the data.
     *
     * @return the file
     */
    protected abstract File getFinalFile();

    /**
     * Tell which file will be used as a staging file and be renamed to {@link #getFinalFile()} once well written.
     *
     * @return the file
     */
    protected abstract File getStagingFile();

    /**
     * Tell the type of the entity to load and save.
     *
     * @return the type
     */
    protected abstract Class<T> getType();

    /**
     * Call once before using if you are not using Spring.
     */
    @PostConstruct
    public synchronized void init() {
        if (load()) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> flush()));
        }
    }

    /**
     * Tell if an entity's key is the one specified.
     *
     * @param key    the key
     * @param entity the entity to check
     * @return true if that is the right entity
     */
    protected abstract boolean isEntity(K key, T entity);

    /**
     * Load if not already.
     *
     * @return true if loaded; false if already loaded
     */
    private synchronized boolean load() {
        if (cachedEntities != null) {
            return false;
        }
        if (getFinalFile().exists()) {
            logger.debug("Loading from file");
            String json = FileTools.getFileAsString(getFinalFile());
            cachedEntities = JsonTools.readFromStringAsList(json, getType());
            previousMd5sum = HashMd5sum.hashString(json);
        } else {
            logger.debug("New state");
            cachedEntities = new ArrayList<>();
        }
        return true;
    }

    /**
     * Remove all entities with the specified key and add the entity.
     *
     * @param key    the key
     * @param entity the entity to add
     */
    public synchronized void update(K key, T entity) {
        delete(key);
        add(entity);
    }

}
