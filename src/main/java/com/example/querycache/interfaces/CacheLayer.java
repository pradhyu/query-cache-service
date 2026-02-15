package com.example.querycache.interfaces;

import java.util.Optional;

public interface CacheLayer<K, V> {
    Optional<V> get(K key);
    void set(K key, V value);
    String getName();
}
