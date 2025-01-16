/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.collection;

import java.util.Map;
import java.util.Objects;

public class ImmutableMapEntry<K, V> implements Map.Entry<K, V>, Comparable<Map.Entry<K, V>> {

    private final K key;
    private final V value;

    public ImmutableMapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableMapEntry<?, ?> that = (ImmutableMapEntry<?, ?>) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public int compareTo(Map.Entry<K, V> o) {
        return key.toString().compareTo(o.getKey().toString());
    }
}
