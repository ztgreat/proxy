package com.proxy.common.cache;

import java.util.Map;

/**
 * @author  ztgreat
 */
public interface Cache<K, V> {


    /**
     * 通过 key 获取数据
     * @param key
     * @return
     * @throws RuntimeException
     */
    V  get(K key) throws RuntimeException;

    /**
     * 保存 key-value
     * @param key
     * @param value
     * @return
     * @throws RuntimeException
     */
    V put(K key, V value) throws RuntimeException;

    /**
     * remove key
     * @param key
     * @return
     * @throws RuntimeException
     */
    V remove(K key) throws RuntimeException;

    /**
     * 获取cache size
     * @return
     * @throws RuntimeException
     */
    long size() throws RuntimeException;

    /**
     * 清空cache
     * @return
     * @throws RuntimeException
     */
    void clear() throws RuntimeException;

    /**
     * 获取全部数据
     * @return
     * @throws RuntimeException
     */
    Map<K,V>getAll() throws  RuntimeException;




}
