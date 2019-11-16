package com.proxy.common.cache;

/**
 * 缓存管理器
 *
 * @author ztgreat
 */
public interface CacheManager<K, V> {

    /**
     * 得到缓存容器
     *
     * @return
     */
    Cache getCache(String name);

}
