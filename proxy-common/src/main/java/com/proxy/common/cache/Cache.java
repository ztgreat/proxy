package com.proxy.common.cache;

import java.util.Map;

/**
 * @author ztgreat
 */
public interface Cache<K, V> {


    /**
     * 通过 key 获取数据
     */
    V get(K key) throws RuntimeException;

    /**
     * 保存 key-value
     */
    V put(K key, V value) throws RuntimeException;

    /**
     * remove key
     */
    V remove(K key) throws RuntimeException;

    /**
     * 获取cache size
     */
    long size() throws RuntimeException;

    /**
     * 清空cache
     */
    void clear() throws RuntimeException;

    /**
     * 获取全部数据
     */
    Map<K, V> getAll() throws RuntimeException;


}
