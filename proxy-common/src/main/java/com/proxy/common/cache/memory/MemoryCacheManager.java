package com.proxy.common.cache.memory;

import com.proxy.common.cache.Cache;
import com.proxy.common.cache.CacheManager;

/**
 * 内存缓存管理器
 *
 * @author ztgreat
 */
public class MemoryCacheManager<K, V> implements CacheManager {


    @Override
    public Cache getCache(String name) {
        return new MemoryCache<K, V>(name);
    }
}
