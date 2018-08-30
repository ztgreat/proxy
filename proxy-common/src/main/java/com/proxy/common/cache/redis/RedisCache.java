package com.proxy.common.cache.redis;

import com.proxy.common.cache.Cache;

import java.util.Map;
import java.util.Set;

/**
 *使用redis 缓存 
 *@author ztgreat
 */
public class  RedisCache<K,V> implements Cache<K,V> {


	/**
	 * redis 客户端
	 */
	private RedisClient redisClient;

	/**
	 * 缓存名称
	 */
	private String name;

	public RedisCache(RedisClient redisClient) {
		this.redisClient = redisClient;
	}

	public RedisCache(RedisClient redisClient, String name) {
		this.redisClient = redisClient;
		this.name = name;
	}

	public RedisCache(String name) {
		this.redisClient = new JedisPoolClient();
		this.name = name;
	}

	public RedisClient getRedisClient() {
		return redisClient;
	}

	@Override
	public V get(K key) throws RuntimeException {
		return null;
	}

	@Override
	public V put(K key, V value) throws RuntimeException {
		return null;
	}

	@Override
	public V remove(K key) throws RuntimeException {
		return null;
	}

	@Override
	public long size() throws RuntimeException {
		return 0;
	}

	@Override
	public void clear() throws RuntimeException {

	}

	@Override
	public Map<K, V> getAll() throws RuntimeException {
		return null;
	}
}
