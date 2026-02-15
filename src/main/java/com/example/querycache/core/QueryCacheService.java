package com.example.querycache.core;

import com.example.querycache.interfaces.CacheLayer;
import com.example.querycache.interfaces.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class QueryCacheService<Q, R> implements DataSource<Q, R> {

    private static final Logger logger = LoggerFactory.getLogger(QueryCacheService.class);

    private final DataSource<Q, R> origin;
    private final List<CacheLayer<String, R>> layers;
    private final CacheKeyGenerator<Q, String> keyGenerator;

    public QueryCacheService(DataSource<Q, R> origin, List<CacheLayer<String, R>> layers,
            CacheKeyGenerator<Q, String> keyGenerator) {
        this.origin = origin;
        this.layers = layers;
        this.keyGenerator = keyGenerator;
    }

    @Override
    public R query(Q query) {
        String key = keyGenerator.generateKey(query);
        logger.info("[QueryCache] Generated key: {}", key);

        // 1. Check Cache Layers
        for (int i = 0; i < layers.size(); i++) {
            CacheLayer<String, R> layer = layers.get(i);
            Optional<R> cachedValue = layer.get(key);

            if (cachedValue.isPresent()) {
                logger.info("[QueryCache] HIT in layer: {}", layer.getName());

                // Backfill previous layers (Read-Repair)
                R value = cachedValue.get();
                for (int j = 0; j < i; j++) {
                    logger.info("[QueryCache] Backfilling layer: {}", layers.get(j).getName());
                    layers.get(j).set(key, value);
                }
                return value;
            } else {
                logger.info("[QueryCache] MISS in layer: {}", layer.getName());
            }
        }

        // 2. Fetch from Origin
        logger.info("[QueryCache] Fetching from origin DataSource...");
        R result = origin.query(query);

        // 3. Populate All Layers
        for (CacheLayer<String, R> layer : layers) {
            logger.info("[QueryCache] Populating layer: {}", layer.getName());
            layer.set(key, result);
        }

        return result;
    }
}
