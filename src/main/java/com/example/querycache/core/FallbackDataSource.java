package com.example.querycache.core;

import com.example.querycache.interfaces.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FallbackDataSource<Q, R> implements DataSource<Q, R> {

    private static final Logger logger = LoggerFactory.getLogger(FallbackDataSource.class);

    private final List<DataSource<Q, R>> sources;

    public FallbackDataSource(List<DataSource<Q, R>> sources) {
        this.sources = sources;
    }

    @Override
    public R query(Q query) {
        for (int i = 0; i < sources.size(); i++) {
            try {
                logger.info("[FallbackDataSource] Trying source #{}", i + 1);
                return sources.get(i).query(query);
            } catch (Exception e) {
                logger.warn("[FallbackDataSource] Source #{} failed: {}", i + 1, e.getMessage());
            }
        }
        throw new RuntimeException("All data sources failed");
    }
}
