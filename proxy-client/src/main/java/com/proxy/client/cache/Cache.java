package com.proxy.client.cache;


/**
 * 缓存
 */
public interface Cache<K, V> {

    /**
     * 获取缓存数据
     */
    V get(K key);

    /**
     * 保存缓存数据
     */
    void put(K key, V v);

    /**
     * 移除某个缓存数据
     */
    void remove(K key);

    /**
     * 清空缓存
     */
    void clear();
}
