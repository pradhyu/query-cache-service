package com.example.querycache.layers;

import com.example.querycache.interfaces.CacheLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCacheLayer<K, V> implements CacheLayer<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCacheLayer.class);

    private final String name;
    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public InMemoryCacheLayer(String name) {
        this.name = name;
    }

    @Override
    public Optional<V> get(K key) {
        logger.debug("[{}] Checking cache for key: {}", name, key);
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public void set(K key, V value) {
        logger.debug("[{}] Setting cache for key: {}", name, key);
        cache.put(key, value);
    }

    @Override
    public String getName() {
        return name;
    }
}
