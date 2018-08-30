package com.proxy.common.cache.redis;

import com.proxy.common.cache.Cache;
import com.proxy.common.cache.CacheManager;
import com.proxy.common.cache.memory.MemoryCache;

/**
 * 内存缓存管理器
 * @author ztgreat
 */
public class RedisCacheManager<K, V> implements CacheManager {


    @Override
    public Cache getCache(String name) {
        return new RedisCache<K,V>(name);
    }
}
