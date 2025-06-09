package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Tools to work with collections.
 */
public final class CollectionsTools {

    /**
     * A collector that will collect to an {@link ArrayList} (to be modifiable).
     *
     * @param <T> the type of the elements
     * @return the collector
     */
    public static <T> Collector<T, ?, ArrayList<T>> collectToArrayList() {
        return Collectors.toCollection(ArrayList::new);
    }

    /**
     * Get a value from a map or insert an empty object for that value.
     *
     * @param map   the map
     * @param key   the key
     * @param clazz the class to instantiate to create an empty object
     * @param <K>   type of the key
     * @param <V>   type of the value
     * @return the value or the new empty value
     */
    public static <K, V> V getOrCreateEmpty(Map<K, V> map, K key, Class<V> clazz) {
        V value = map.get(key);
        if (value == null) {
            try {
                value = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new SmallToolsException("Could not create the empty object", e);
            }
            map.put(key, value);
        }

        return value;
    }

    /**
     * Get a value from a map or insert an empty {@link ArrayList} for that value.
     *
     * @param map   the map
     * @param key   the key
     * @param clazz the class of the values in the list
     * @param <K>   type of the key
     * @param <V>   type of the value in the list
     * @return the value or the new empty value
     */
    public static <K, V> List<V> getOrCreateEmptyArrayList(Map<K, List<V>> map, K key, Class<V> clazz) {
        return map.computeIfAbsent(key, k -> new ArrayList<>());
    }

    /**
     * Get a value from a map or insert an empty {@link HashSet} for that value.
     *
     * @param map   the map
     * @param key   the key
     * @param clazz the class of the values in the list
     * @param <K>   type of the key
     * @param <V>   type of the value in the list
     * @return the value or the new empty value
     */
    public static <K, V> Set<V> getOrCreateEmptyHashSet(Map<K, Set<V>> map, K key, Class<V> clazz) {
        return map.computeIfAbsent(key, k -> new HashSet<>());
    }

    /**
     * Get a value from a map or insert an empty {@link TreeSet} for that value.
     *
     * @param map   the map
     * @param key   the key
     * @param clazz the class of the values in the list
     * @param <K>   type of the key
     * @param <V>   type of the value in the list
     * @return the value or the new empty value
     */
    public static <K, V extends Comparable<V>> Set<V> getOrCreateEmptyTreeSet(Map<K, Set<V>> map, K key, Class<V> clazz) {
        return map.computeIfAbsent(key, k -> new TreeSet<>());
    }

    /**
     * Is true if all the items are not null.
     *
     * @param items the items
     * @return true if all are not null
     */
    public static boolean isAllItemNotNull(Collection<?> items) {
        for (Object item : items) {
            if (item == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is true if all the items are not null.
     *
     * @param items the items
     * @return true if all are not null
     */
    public static boolean isAllItemNotNull(Object... items) {
        for (Object item : items) {
            if (item == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is true if all the items are not null and not empty.
     *
     * @param items the items
     * @return true if all are not null and not empty
     */
    public static boolean isAllItemNotNullOrEmpty(Collection<String> items) {
        for (String item : items) {
            if (item == null || item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is true if all the items are not null and not empty.
     *
     * @param items the items
     * @return true if all are not null and not empty
     */
    public static boolean isAllItemNotNullOrEmpty(String... items) {
        for (String item : items) {
            if (item == null || item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is true if any of the items is not null.
     *
     * @param items the items
     * @return true if any is not null
     */
    public static boolean isAnyItemNotNull(Collection<?> items) {
        for (Object item : items) {
            if (item != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is true if any of the items is not null.
     *
     * @param items the items
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
     * Is true if any of the items is not null and not empty.
     *
     * @param items the items
     * @return true if any is not null and not empty
     */
    public static boolean isAnyItemNotNullOrEmpty(Collection<String> items) {
        for (String item : items) {
            if (item != null && !item.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is true if any of the items is not null and not empty.
     *
     * @param items the items
     * @return true if any is not null and not empty
     */
    public static boolean isAnyItemNotNullOrEmpty(String... items) {
        for (String item : items) {
            if (item != null && !item.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells if the collection is null or empty.
     *
     * @param collection the collection to check
     * @return true if is null or empty
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Remove the first entry with the specified value in the map.
     *
     * @param map           the map
     * @param valueToRemove the value to remove the entry
     * @param <K>           the type of the key
     * @param <V>           the type of the value
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
     * @param map           the map
     * @param valueToRemove the value to remove the entries
     * @param <K>           the type of the key
     * @param <V>           the type of the value
     */
    public static <K, V> void removeValues(Map<K, V> map, V valueToRemove) {
        map.entrySet().removeIf(kvEntry -> valueToRemove == kvEntry.getValue());
    }

    private CollectionsTools() {
    }

}
