package com.proxy.common.cache.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.Set;

/**
 * redis单机版客户端
 * @author ztgreat
 */
public class JedisPoolClient implements RedisClient {

	private JedisPool jedisPool;

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public void closeResource(Jedis jedis) {
		if (jedis == null)
			return;
		jedis.close();
	}

	@Override
	public RedisClient getRedisClient() {
		return this;
	}

	public void selectDbIndex(Jedis jedis, Integer dbIndex) {
		if (dbIndex != null) {
			jedis.select(dbIndex);
		}
	}

	@Override
	public byte[] get(byte[] key, Integer dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			byte[] result = jedis.get(key);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}

	}

	@Override
	public String set(byte[] key, byte[] value, Integer dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			String string = jedis.set(key, value);
			return string;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}
	}

	@Override
	public String set(byte[] key, byte[] value, int second, Integer dbIndex) {

		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			String string = jedis.set(key, value);
			jedis.expire(key, second);
			return string;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}

	}

	@Override
	public long incr(byte[] key, Integer dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			Long result = jedis.incr(key);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}
	}

	@Override
	public Long hset(byte[] hkey, byte[] key, byte[] value, Integer dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			Long hset = jedis.hset(hkey, key, value);
			return hset;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}
	}

	@Override
	public byte[] hget(byte[] hkey, byte[] key, Integer dbIndex) {
		Jedis jedis = null;
		try {

			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			byte[] result = jedis.hget(hkey, key);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}
	}

	@Override
	public Long del(byte[] key, Integer dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			Long result = jedis.del(key);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}
	}

	@Override
	public Long hdel(byte[] hkey, byte[] key, Integer dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			Long result = jedis.hdel(hkey, key);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}
	}

	@Override
	public Long expire(byte[] key, int second, Integer dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			Long result = jedis.expire(key, second);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}
	}

	@Override
	public Set<byte[]> keys(byte[] pattern, Integer dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			selectDbIndex(jedis, dbIndex);
			Set<byte[]> keys = jedis.keys(pattern);
			return keys;
		} catch (Exception e) {
			throw e;
		} finally {
			closeResource(jedis);
		}
	}

}
