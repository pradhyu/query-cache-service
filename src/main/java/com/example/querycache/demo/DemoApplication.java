package com.example.querycache.demo;

import com.example.querycache.core.CacheKeyGenerator;
import com.example.querycache.core.EdiStyleKeyGenerator;
import com.example.querycache.core.QueryCacheService;
import com.example.querycache.interfaces.CacheLayer;
import com.example.querycache.interfaces.DataSource;
import com.example.querycache.layers.InMemoryCacheLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.querycache")
public class DemoApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    private final EdiStyleKeyGenerator keyGenerator;

    public DemoApplication(EdiStyleKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Reversible Key Demo...");

        // --- 1. Construct Cache ---
        MockDataSource origin = new MockDataSource();
        CacheLayer<String, String> l1Cache = new InMemoryCacheLayer<>("L1-Memory");
        List<CacheLayer<String, String>> layers = List.of(l1Cache);
        // Use the interface type for the service
        DataSource<Map<String, Object>, String> queryCache = new QueryCacheService<Map<String, Object>, String>(origin,
                layers, (CacheKeyGenerator) keyGenerator);

        // --- 2. Demonstrate Standard Query (Default Columns) ---
        Map<String, Object> queryStandard = Map.of(
                "table", "users",
                "id", 123);
        logger.info("\n=== 1. Standard Query (Implicit *) ===");
        logger.info("Query: {}", queryStandard);
        String keyStandard = keyGenerator.generateKey(queryStandard);
        logger.info("Key:   {}", keyStandard); // Expected: users|id=123
        logger.info("Reconstructed: {}", keyGenerator.reconstructQuery(keyStandard));

        // --- 3. Demonstrate Specific Columns ---
        Map<String, Object> querySpecific = Map.of(
                "table", "users",
                "id", 123,
                "columns", "id,name");
        logger.info("\n=== 2. Specific Columns Query ===");
        logger.info("Query: {}", querySpecific);
        String keySpecific = keyGenerator.generateKey(querySpecific);
        logger.info("Key:   {}", keySpecific); // Expected: users|id=123|id,name
        logger.info("Reconstructed: {}", keyGenerator.reconstructQuery(keySpecific));

        // --- 4. Demonstrate Multiple WHERE Clauses (New) ---
        Map<String, Object> queryMulti = Map.of(
                "table", "orders",
                "status", "SHIPPED",
                "region", "US-EAST",
                "min_amount", 100);
        logger.info("\n=== 3. Multiple WHERE Clauses Query ===");
        logger.info("Query: {}", queryMulti);
        String keyMulti = keyGenerator.generateKey(queryMulti);
        logger.info("Key:   {}", keyMulti); // Expected: orders|min_amount=100;region=US-EAST;status=SHIPPED
        logger.info("Reconstructed: {}", keyGenerator.reconstructQuery(keyMulti));

        // --- 5. Run Query Logic ---
        logger.info("\n=== Running Query with Canonical Key ===");
        queryCache.query(queryMulti);
    }
}
