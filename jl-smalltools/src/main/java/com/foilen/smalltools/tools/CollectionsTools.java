/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class CollectionsTools {

    /**
     * Remove the first entry with the specified value in the map.
     * 
     * @param map
     *            the map
     * @param valueToRemove
     *            the value to remove the entry
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
