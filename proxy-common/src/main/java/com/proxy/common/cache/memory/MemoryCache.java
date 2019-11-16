package com.proxy.common.cache.memory;

import com.proxy.common.cache.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存缓存
 *
 * @author ztgreat
 */
public class MemoryCache<K, V> implements Cache<K, V> {

    /**
     * 使用ConcurrentHashMap 作为内存缓存容器
     */
    private Map<K, V> cache;

    /**
     * 缓存名称
     */
    private String name;

    public MemoryCache(String name) {
        this.cache = new ConcurrentHashMap<K, V>();
    }

    @Override
    public V get(K key) throws RuntimeException {
        return cache.get(key);
    }

    @Override
    public V put(K key, V value) throws RuntimeException {
        return cache.put(key, value);
    }

    @Override
    public V remove(K key) throws RuntimeException {
        return cache.remove(key);
    }

    @Override
    public long size() throws RuntimeException {
        return cache.size();
    }

    @Override
    public void clear() throws RuntimeException {
        cache.clear();
    }

    @Override
    public Map<K, V> getAll() throws RuntimeException {
        return cache;
    }
}
