/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.foilen.smalltools.exception.SmallToolsException;

public final class CollectionsTools {

    /**
     * Get a value from a map or insert an empty object for that value.
     * 
     * @param map
     *            the map
     * @param key
     *            the key
     * @param clazz
     *            the class to instantiate to create an empty object
     * @param <K>
     *            type of the key
     * @param <V>
     *            type of the value
     * @return the value or the new empty value
     */
    public static <K, V> V getOrCreateEmpty(Map<K, V> map, K key, Class<V> clazz) {
        V value = map.get(key);
        if (value == null) {
            try {
                value = clazz.newInstance();
            } catch (Exception e) {
                throw new SmallToolsException("Could not create the empty object", e);
            }
            map.put(key, value);
        }

        return value;
    }

    /**
     * Is true if any of the items is not null.
     * 
     * @param items
     *            the items
     * @return true if any is not null
     */
    public static boolean isAnyItemNotNull(Object... items) {
        for (Object item : items) {
            if (item != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells if the collection is null or empty.
     * 
     * @param collection
     *            the collection to check
     * @return true if is null or empty
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Remove the first entry with the specified value in the map.
     * 
     * @param map
     *            the map
     * @param valueToRemove
     *            the value to remove the entry
     * @param <K>
     *            the type of the key
     * @param <V>
     *            the type of the value
     * @return the removed key or null if not found
     */
    public static <K, V> K removeValue(Map<K, V> map, V valueToRemove) {
        Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            if (valueToRemove == entry.getValue()) {
                K key = entry.getKey();
                iterator.remove();
                return key;
            }
        }

        return null;
    }

    /**
     * Remove the entries with the specified value in the map.
     * 
     * @param map
     *            the map
     * @param valueToRemove
     *            the value to remove the entries
     * @param <K>
     *            the type of the key
     * @param <V>
     *            the type of the value
     */
    public static <K, V> void removeValues(Map<K, V> map, V valueToRemove) {
        Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            if (valueToRemove == iterator.next().getValue()) {
                iterator.remove();
            }
        }
    }

    private CollectionsTools() {
    }

}
