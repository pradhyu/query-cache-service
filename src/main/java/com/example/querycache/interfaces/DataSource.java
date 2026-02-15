package com.example.querycache.interfaces;

public interface DataSource<Q, R> {
    R query(Q query);
}
