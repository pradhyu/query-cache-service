# Multi-Layer Query Cache

A robust, generic caching library for Java applications designed to handle complex query objects (Maps, SQL, etc.) with flexible caching strategies and human-readable keys.

## Features

-   **Multi-Layer Architecture**: Chain multiple cache layers (Memory, Redis, Disk) before hitting the origin.
-   **Generic Design**: Works with any Query type (`Q`) and any Result type (`R`).
-   **Compact, Reversible Keys**: Generates EDI-style positional keys (e.g., `users|id=123`) that are readable and can be parsed back into the original query.
-   **Resilience**: Supports fallback data sources and read-repair (populating L1 from L2).

## Architecture

The system follows a Chain of Responsibility pattern managed by the `QueryCacheService`.

```mermaid
graph TD
    Client[Client Application] -->|Query Q| Service[QueryCacheService]
    
    subgraph "Key Generation"
        Service -->|Q| KG[CacheKeyGenerator]
        KG -->|Returns Key K| Service
    end
    
    Service -->|Get K| L1[L1: Memory Cache]
    L1 -->|Miss| L2[L2: Distributed Cache]
    L2 -->|Miss| Origin[Origin Data Source]
    
    Origin -->|Result R| L2
    L2 -->|Put K, R| L1
    L1 -->|Put K, R| Service
    Service -->|Return R| Client
    
    style Service fill:#eceff1,stroke:#333,stroke-width:2px,color:#000
    style KG fill:#d1c4e9,stroke:#333,color:#000
    style Origin fill:#c8e6c9,stroke:#333,color:#000
    style Client fill:#bbdefb,stroke:#333,color:#000
    style L1 fill:#f5f5f5,stroke:#333,color:#000
    style L2 fill:#e3f2fd,stroke:#333,color:#000
    
    linkStyle default stroke:#333,stroke-width:1px,color:#000
```

## Compact Key Strategy (EDI-Style)

The default `EdiStyleKeyGenerator` prevents cache pollution and ensures readability by creating keys that are **Compact**, **Positional**, and **Deterministic**.

**Format:** `TABLE | FILTERS | OPTIONAL_COLUMNS`

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'signalTextColor': '#000000', 'actorTextColor': '#000000', 'noteTextColor': '#000000'}}}%%
sequenceDiagram
    participant Query as Query Map
    participant KG as KeyGenerator
    participant Key as Cache Key String
    
    Note over Query: {table=users, id=123}
    Query->>KG: generateKey(map)
    KG->>KG: 1. Extract Table (users)
    KG->>KG: 2. Extract Filters (id=123)
    KG->>KG: 3. Check specific columns? (No)
    KG->>Key: Returns "users|id=123"
    
    Note over Query: {table=orders, status=OPEN, min=50}
    Query->>KG: generateKey(map)
    KG->>KG: 1. Extract Table (orders)
    KG->>KG: 2. Sort Filters (min=50,status=OPEN)
    KG->>Key: Returns "orders|min=50,status=OPEN"
```

## Usage

### 1. Define Dependencies

```java
// 1. Origin Source
DataSource<Map<String, Object>, String> origin = new MyDatabaseSource();

// 2. Cache Layers
CacheLayer<String, String> memoryCache = new InMemoryCacheLayer<>("L1");

// 3. Key Generator
CacheKeyGenerator keyGen = new EdiStyleKeyGenerator();
```

### 2. Initialize Service

```java
DataSource<Map<String, Object>, String> cacheService = 
    new QueryCacheService<>(origin, List.of(memoryCache), (CacheKeyGenerator) keyGen);
```

### 3. query

```java
Map<String, Object> query = Map.of(
    "table", "users",
    "id", 123
);

String result = cacheService.query(query);
// Key generated: "users|id=123"
```
