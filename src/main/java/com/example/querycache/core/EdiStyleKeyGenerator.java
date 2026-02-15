package com.example.querycache.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class EdiStyleKeyGenerator implements CacheKeyGenerator<Object, String> {

    private static final Logger logger = LoggerFactory.getLogger(EdiStyleKeyGenerator.class);
    // Delimiter for positions
    private static final String POS_DELIMITER = "\\|";
    private static final String POS_DELIMITER_CHAR = "|";

    // Delimiter for KV pairs in filter section
    private static final String KV_DELIMITER = ";";

    /**
     * Generates a compact, positional key: TABLE|FILTERS[|COLUMNS]
     */
    public String generateKey(Object query) {
        String key;
        if (query instanceof Map) {
            key = generateMapKey((Map<?, ?>) query);
        } else {
            key = "OBJ|" + query.toString().replaceAll("\\|", "/");
        }

        logger.info("Generated Compact Key: {}", key);
        return key;
    }

    private String generateMapKey(Map<?, ?> map) {
        // 1. Extract Table (Required, Position 0)
        String table = "UNKNOWN";
        if (map.containsKey("table")) {
            table = String.valueOf(map.get("table"));
        }

        // 2. Extract Columns (Optional, Position 2)
        String columns = "*";
        if (map.containsKey("columns")) {
            columns = String.valueOf(map.get("columns"));
        } else if (map.containsKey("select")) {
            columns = String.valueOf(map.get("select"));
        }

        // 3. Extract Filters (Rest of keys, Position 1)
        TreeMap<String, String> filters = new TreeMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String k = String.valueOf(entry.getKey());
            // Filter out system/meta keys
            if (k.equals("table") || k.equals("columns") || k.equals("select")) {
                continue;
            }
            filters.put(k, String.valueOf(entry.getValue()));
        }

        String filterString = filters.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(KV_DELIMITER));

        // Assemble: TABLE|FILTERS
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(table).append(POS_DELIMITER_CHAR).append(filterString);

        // Append |COLUMNS only if not default "*"
        if (!"*".equals(columns) && !"ALL".equals(columns)) {
            keyBuilder.append(POS_DELIMITER_CHAR).append(columns);
        }

        return keyBuilder.toString();
    }

    /**
     * Reconstructs the original query object from the compact key.
     */
    public Object reconstructQuery(String key) {
        // Split into at most 3 parts: Table, Filters, Columns
        // If split is 2, columns implies "*"
        String[] parts = key.split(POS_DELIMITER);

        Map<String, String> map = new TreeMap<>();

        // Pos 0: Table
        if (parts.length >= 1) {
            map.put("table", parts[0]);
        }

        // Pos 1: Filters
        if (parts.length >= 2 && !parts[1].isEmpty()) {
            String[] pairs = parts[1].split(KV_DELIMITER);
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    map.put(kv[0], kv[1]);
                }
            }
        }

        // Pos 2: Columns (Optional)
        if (parts.length >= 3) {
            map.put("columns", parts[2]);
        }

        return map;
    }
}
