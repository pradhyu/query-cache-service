package com.example.querycache.demo;

import com.example.querycache.interfaces.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

public class MockDataSource implements DataSource<Map<String, Object>, String> {

    private static final Logger logger = LoggerFactory.getLogger(MockDataSource.class);

    @Override
    public String query(Map<String, Object> query) {
        logger.info("[MockDataSource] Processing query: {}", query);
        try {
            // Simulate network latency
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Result for " + query.toString();
    }
}
