package com.example.querycache.core;

/**
 * Strategy interface for generating cache keys from queries.
 * 
 * @param <Q> The type of the query object
 * @param <K> The type of the cache key (usually String)
 */
public interface CacheKeyGenerator<Q, K> {

    /**
     * Generates a unique cache key for the given query.
     */
    K generateKey(Q query);

    /**
     * Optional: Reconstructs the query from the key.
     * Useful for debugging or cache analysis.
     * 
     * @throws UnsupportedOperationException if not implemented
     */
    default Q reconstructQuery(K key) {
        throw new UnsupportedOperationException("Reconstruction not supported by this generator");
    }
}
